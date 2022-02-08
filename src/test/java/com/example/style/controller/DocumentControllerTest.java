package com.example.style.controller;

import com.example.style.request.DocumentRequest;
import com.example.style.util.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class DocumentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void init() {
        String insertToDocument = "INSERT INTO DOCUMENT(ID, TITLE, CLASSIFICATION, CONTENT, STATUS, WRITER) VALUES(1, '제목', '분류', '내용', 'created', '홍길동')";
        String insertToMember = "INSERT INTO MEMBER(ID, USERNAME, PASSWORD) values(1, '홍길동', '1234')";
        String insertToApproval = "INSERT INTO APPROVAL(ID, APPROVER_ID, DOCUMENT_ID, COMMENT, ISDONE) values(1, 1, 1, null, false)";
        jdbcTemplate.update(insertToDocument);
        jdbcTemplate.update(insertToMember);
        jdbcTemplate.update(insertToApproval);

        CustomUserDetails userDetails = new CustomUserDetails("홍길동", "1234");
        Authentication mockAuthentication = new TestingAuthenticationToken(userDetails, null, (List<GrantedAuthority>) userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(mockAuthentication);
    }

    @AfterEach
    void destroy() {
        String resetApproval = "DELETE FROM APPROVAL";
        String resetApprovalSequence = "ALTER TABLE APPROVAL ALTER COLUMN `ID` RESTART WITH 1";
        String resetDocument = "DELETE FROM DOCUMENT";
        String resetDocumentSequence = "ALTER TABLE DOCUMENT ALTER COLUMN `ID` RESTART WITH 1";
        String resetMember = "DELETE FROM MEMBER";
        String resetMemberSequence = "ALTER TABLE MEMBER ALTER COLUMN `ID` RESTART WITH 1";
        jdbcTemplate.update(resetApproval);
        jdbcTemplate.update(resetApprovalSequence);
        jdbcTemplate.update(resetDocument);
        jdbcTemplate.update(resetDocumentSequence);
        jdbcTemplate.update(resetMember);
        jdbcTemplate.update(resetMemberSequence);
    }

    @Test
    @DisplayName("문서 생성")
    void test01() throws Exception {
        List<String> approvals = new ArrayList<>();
        approvals.add("홍길동");
        DocumentRequest request = DocumentRequest.builder()
                .title("제목")
                .classification("분류")
                .content("내용")
                .approvers(approvals)
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(
                    post("/document/create")
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.response.id", is(2)))
                .andExpect(jsonPath("$.response.title", is("제목")))
                .andExpect(jsonPath("$.response.classification", is("분류")))
                .andExpect(jsonPath("$.response.content", is("내용")))
                .andExpect(jsonPath("$.response.status", is("created")))
                .andExpect(jsonPath("$.response.writer", is("홍길동")))
                .andExpect(jsonPath("$.response.approvals[0].id", is(2)))
                .andExpect(jsonPath("$.response.approvals[0].approver.id", is(1)))
                .andExpect(jsonPath("$.response.approvals[0].approver.username", is("홍길동")))
                .andExpect(jsonPath("$.response.approvals[0].approver.password", is("1234")))
                .andExpect(jsonPath("$.response.approvals[0].comment", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
                ;
    }

    @Test
    @DisplayName("문서 승인 - created 일때")
    void test02() throws Exception {
        DocumentRequest request = DocumentRequest.builder()
                .id(1)
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(
                        put("/document/approve")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.response.id", is(1)))
                .andExpect(jsonPath("$.response.title", is("제목")))
                .andExpect(jsonPath("$.response.classification", is("분류")))
                .andExpect(jsonPath("$.response.content", is("내용")))
                .andExpect(jsonPath("$.response.status", is("approved")))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }

    @Test
    @DisplayName("문서 승인 - processed 일때")
    void test03() throws Exception {
        String sql = "UPDATE DOCUMENT SET STATUS='processed' WHERE ID=1";
        jdbcTemplate.update(sql);

        DocumentRequest request = DocumentRequest.builder()
                .id(1)
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(
                        put("/document/approve")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.response.id", is(1)))
                .andExpect(jsonPath("$.response.title", is("제목")))
                .andExpect(jsonPath("$.response.classification", is("분류")))
                .andExpect(jsonPath("$.response.content", is("내용")))
                .andExpect(jsonPath("$.response.status", is("approved")))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }

    @Test
    @DisplayName("문서 승인 - approved 일때")
    void test04() throws Exception {
        String sql = "UPDATE DOCUMENT SET STATUS='approved' WHERE ID=1";
        jdbcTemplate.update(sql);

        DocumentRequest request = DocumentRequest.builder()
                .id(1)
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(
                        put("/document/approve")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", is("이미 결재된 문서입니다.")))
        ;
    }

    @Test
    @DisplayName("문서 승인 - rejected 일때")
    void test05() throws Exception {
        String sql = "UPDATE DOCUMENT SET STATUS='rejected' WHERE ID=1";
        jdbcTemplate.update(sql);

        DocumentRequest request = DocumentRequest.builder()
                .id(1)
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(
                        put("/document/approve")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", is("승인 거부된 문서입니다.")))
        ;
    }

    @Test
    @DisplayName("문서 거부 - created 일때")
    void test06() throws Exception {
        DocumentRequest request = DocumentRequest.builder()
                .id(1)
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(
                        put("/document/reject")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.response.id", is(1)))
                .andExpect(jsonPath("$.response.title", is("제목")))
                .andExpect(jsonPath("$.response.classification", is("분류")))
                .andExpect(jsonPath("$.response.content", is("내용")))
                .andExpect(jsonPath("$.response.status", is("rejected")))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }

    @Test
    @DisplayName("문서 거부 - processed 일때")
    void test07() throws Exception {
        String sql = "UPDATE DOCUMENT SET STATUS='processed' WHERE ID=1";
        jdbcTemplate.update(sql);

        DocumentRequest request = DocumentRequest.builder()
                .id(1)
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(
                        put("/document/reject")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.response.id", is(1)))
                .andExpect(jsonPath("$.response.title", is("제목")))
                .andExpect(jsonPath("$.response.classification", is("분류")))
                .andExpect(jsonPath("$.response.content", is("내용")))
                .andExpect(jsonPath("$.response.status", is("rejected")))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }

    @Test
    @DisplayName("문서 거부 - approved 일때")
    void test08() throws Exception {
        String sql = "UPDATE DOCUMENT SET STATUS='approved' WHERE ID=1";
        jdbcTemplate.update(sql);

        DocumentRequest request = DocumentRequest.builder()
                .id(1)
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(
                        put("/document/reject")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", is("이미 결재된 문서입니다.")))
        ;
    }

    @Test
    @DisplayName("문서 거부 - rejected 일때")
    void test09() throws Exception {
        String sql = "UPDATE DOCUMENT SET STATUS='rejected' WHERE ID=1";
        jdbcTemplate.update(sql);

        DocumentRequest request = DocumentRequest.builder()
                .id(1)
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(
                        put("/document/reject")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", is("이미 승인 거부된 문서입니다.")))
        ;
    }

    @Test
    @DisplayName("문서 조회 - 내가 생성한 문서 중 결재 진행중인 문서")
    void test10() throws Exception {
        String sql = "UPDATE DOCUMENT SET STATUS='processed' WHERE ID=1";
        jdbcTemplate.update(sql);

        mockMvc.perform(
                        get("/document/list")
                                .param("key", "OUTBOX")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.response[0].id", is(1)))
                .andExpect(jsonPath("$.response[0].title", is("제목")))
                .andExpect(jsonPath("$.response[0].classification", is("분류")))
                .andExpect(jsonPath("$.response[0].content", is("내용")))
                .andExpect(jsonPath("$.response[0].status", is("processed")))
                .andExpect(jsonPath("$.response[0].writer", is("홍길동")))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }

}

package com.example.style.repository;

import com.example.style.config.TestDataBaseConfig;
import com.example.style.domain.Document;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@DataJpaTest
@Import(TestDataBaseConfig.class)
public class DocumentRepositoryTest {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("내가 결재를 해야 할 문서")
    void test01() {
        String insertToDocument = "INSERT INTO DOCUMENT(ID, TITLE, CLASSIFICATION, CONTENT, STATUS, WRITER) VALUES(1, '제목', '분류', '내용', 'processed', '홍길동')";
        String insertToMember = "INSERT INTO MEMBER(ID, USERNAME, PASSWORD) values(1, '홍길동', '1234')";
        String insertToApproval = "INSERT INTO APPROVAL(ID, APPROVER_ID, DOCUMENT_ID, COMMENT, ISDONE) values(1, 1, 1, null, false)";
        jdbcTemplate.update(insertToDocument);
        jdbcTemplate.update(insertToMember);
        jdbcTemplate.update(insertToApproval);

        List<Document> list = documentRepository.approvalRequired("홍길동");
        assertThat(list.isEmpty(), is(false));
        assertThat(list, is(IsNull.notNullValue()));
    }

    @Test
    @DisplayName("내가 관여한 문서 중 결재가 완료(승인 또는 거절)된 문서")
    void test02() {
        String insertToDocument = "INSERT INTO DOCUMENT(ID, TITLE, CLASSIFICATION, CONTENT, STATUS, WRITER) VALUES(1, '제목', '분류', '내용', 'processed', '홍길동')";
        String insertToDocument2 = "INSERT INTO DOCUMENT(ID, TITLE, CLASSIFICATION, CONTENT, STATUS, WRITER) VALUES(2, '제목', '분류', '내용', 'approved', '김길동')";
        String insertToMember = "INSERT INTO MEMBER(ID, USERNAME, PASSWORD) values(1, '홍길동', '1234')";
        String insertToMember2 = "INSERT INTO MEMBER(ID, USERNAME, PASSWORD) values(2, '김길동', '1234')";
        String insertToApproval = "INSERT INTO APPROVAL(ID, APPROVER_ID, DOCUMENT_ID, COMMENT, ISDONE) values(1, 1, 1, null, false)";
        String insertToApproval2 = "INSERT INTO APPROVAL(ID, APPROVER_ID, DOCUMENT_ID, COMMENT, ISDONE) values(2, 1, 2, null, true)";
        jdbcTemplate.update(insertToDocument);
        jdbcTemplate.update(insertToDocument2);
        jdbcTemplate.update(insertToMember);
        jdbcTemplate.update(insertToMember2);
        jdbcTemplate.update(insertToApproval);
        jdbcTemplate.update(insertToApproval2);

        List<Document> list = documentRepository.participatedIn("홍길동");
        assertThat(list.isEmpty(), is(false));
        assertThat((long) list.size(), is(1L));
    }
}

package com.example.style.repository.custom;

import com.example.style.domain.Document;
import com.example.style.domain.QApproval;
import com.example.style.domain.QDocument;
import com.example.style.util.Status;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class CustomDocumentRepositoryImpl implements CustomDocumentRepository {

    private final JPAQueryFactory jpaQueryFactory;

    private final QDocument qDocument = QDocument.document;

    private final QApproval qApproval = QApproval.approval;

    //내가 결재를 해야 할 문서
    @Override
    public List<Document> approvalRequired(String username) {
        List<Tuple> list = jpaQueryFactory
                .select(qDocument, qApproval)
                .from(qDocument)
                .join(qApproval)
                .on(qDocument.id.eq(qApproval.document.id))
                .where(qApproval.approver.username.eq(username), qApproval.isDone.ne(true))
                .fetch()
                ;
        List<Document> documents = new ArrayList<>();
        for (Tuple t : list) {
            Document document = t.get(0, Document.class);
            documents.add(document);
        }
        log.debug("documents ==> {}", documents);
        return documents;
    }

    // 내가 관여한 문서 중 결재가 완료(승인 또는 거절)된 문서
    @Override
    public List<Document> participatedIn(String username) {
        List<Tuple> list = jpaQueryFactory
                .select(qDocument, qApproval)
                .from(qDocument)
                .join(qApproval)
                .on(qDocument.id.eq(qApproval.document.id))
                .where(qDocument.writer.eq(username).and( qApproval.isDone.eq(true) ).or( qApproval.approver.username.eq(username).and( qApproval.isDone.eq(true).and(qDocument.status.eq(Status.approved).or( qDocument.status.eq(Status.rejected) )) ) ))
                .fetch()
                ;
        log.debug("tuples ==> {}", list);
        List<Document> documents = new ArrayList<>();
        for (Tuple t : list) {
            Document document = t.get(0, Document.class);
            documents.add(document);
        }
        log.debug("documents ==> {}", documents);
        return documents;
    }
}

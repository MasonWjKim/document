package com.example.style.repository;

import com.example.style.domain.Document;
import com.example.style.repository.custom.CustomDocumentRepository;
import com.example.style.util.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, CustomDocumentRepository {

    List<Document> findByWriterAndStatusOrStatus(String username, Status created, Status processed);
}

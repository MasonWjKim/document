package com.example.style.repository.custom;

import com.example.style.domain.Document;

import java.util.List;

public interface CustomDocumentRepository {

    List<Document> approvalRequired(String username);

    List<Document> participatedIn(String username);

}

package com.example.style.request;

import com.example.style.util.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
@ToString
public class DocumentRequest {

    private long id;

    private String title;

    private String classification;

    private String content;

    private Status status;

    private List<String> approvers;

    private String comment;

}

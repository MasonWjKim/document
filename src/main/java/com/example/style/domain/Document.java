package com.example.style.domain;

import com.example.style.util.Status;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "approvals")
@Entity
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String title;

    @Column
    private String classification;

    @Column
    private String content;

    @Enumerated(EnumType.STRING)
    @Column
    private Status status;

    @Column
    private String writer;

    @JsonManagedReference
    @OneToMany(mappedBy = "document")
    private List<Approval> approvals;

}

package com.example.style.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
public class Approval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn
    private Member approver;

    @JsonBackReference
    @ManyToOne
    @JoinColumn
    private Document document;

    @Column
    private String comment;

    @Column(name = "isdone")
    private boolean isDone;

}

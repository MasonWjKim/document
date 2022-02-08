package com.example.style.domain;

import lombok.*;

import javax.persistence.*;
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column
  private String username;

  @Column
  private String password;

}

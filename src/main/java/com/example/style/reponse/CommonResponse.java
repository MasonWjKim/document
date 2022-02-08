package com.example.style.reponse;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
public class CommonResponse<T> {

  private int status;

  private T response;

  private ErrorResponse error;

}

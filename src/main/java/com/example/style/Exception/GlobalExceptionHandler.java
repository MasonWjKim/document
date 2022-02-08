package com.example.style.Exception;

import com.example.style.reponse.CommonResponse;
import com.example.style.reponse.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(value=StyleException.class)
	public ResponseEntity<?> handleStyleException(StyleException e) {
		CommonResponse<Object> response = CommonResponse.builder()
				.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.error(new ErrorResponse(e.getMessage()))
				.build();
		log.error("Error Message : {}", e.getMessage());
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
}

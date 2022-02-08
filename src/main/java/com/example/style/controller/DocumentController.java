package com.example.style.controller;

import com.example.style.domain.Document;
import com.example.style.reponse.CommonResponse;
import com.example.style.request.DocumentRequest;
import com.example.style.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/document")
public class DocumentController {

	private final DocumentService documentService;

	/**
	 * 1. 문서 생성
	 */
	@PostMapping("/create")
	public ResponseEntity<?> create(Principal principal, @RequestBody DocumentRequest request, BindingResult bindingResult) {
		String username = principal.getName();

		log.debug("request : {}", request);
		Document document = documentService.create(username, request);

		return responseOK(document);
	}

	/*
	 * 2. 문서 결재
	 */
	@PutMapping("/approve")
	public ResponseEntity<?> approve(Principal principal, @RequestBody DocumentRequest request, BindingResult bindingResult) {
		String username = principal.getName();

		Document document = documentService.approve(username, request);

		return responseOK(document);
	}

	/*
	 * 3. 문서 거부
	 */
	@PutMapping("/reject")
	public ResponseEntity<?> reject(Principal principal, @RequestBody DocumentRequest request, BindingResult bindingResult) {
		String username = principal.getName();

		Document document = documentService.reject(username, request);

		return responseOK(document);
	}

	/*
	 * 4. 문서 조회
	 */
	@GetMapping("/list")
	public ResponseEntity<?> list(Principal principal, String key) {
		log.debug("key : {}", key);
		String username = principal.getName();

		List<Document> documents = documentService.list(username, key);

		return responseOK(documents);
	}

	private ResponseEntity<?> responseOK(Object input) {
		CommonResponse<Object> response = CommonResponse.builder()
				.status(HttpStatus.OK.value())
				.response(input)
				.build();

		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
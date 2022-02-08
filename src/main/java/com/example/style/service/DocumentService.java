package com.example.style.service;

import com.example.style.Exception.StyleException;
import com.example.style.domain.Approval;
import com.example.style.domain.Document;
import com.example.style.domain.Member;
import com.example.style.repository.ApprovalRepository;
import com.example.style.repository.DocumentRepository;
import com.example.style.repository.MemberRepository;
import com.example.style.request.DocumentRequest;
import com.example.style.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RequiredArgsConstructor
@Service
public class DocumentService {

	private final DocumentRepository documentRepository;

	private final ApprovalRepository approvalRepository;

	private final MemberRepository memberRepository;

	@Transactional
	public Document create(String username, DocumentRequest request) {
		Document document = Document.builder()
				.title(request.getTitle())
				.classification(request.getClassification())
				.content(request.getContent())
				.status(Status.created)
				.writer(username)
				.build();

		Document ret = documentRepository.save(document);

		List<String> approvers = request.getApprovers();
		if (approvers == null) {
			throw new StyleException("지정된 결재자가 없습니다.");
		}
		List<Approval> approvals = new ArrayList<>();
		approvers.forEach((a) -> {
			Member member = memberRepository.findByUsername(a);
			Approval approval = Approval.builder()
					.document(ret)
					.approver(member)
					.isDone(false)
					.build();
			approvals.add(approvalRepository.save(approval));
		});
		ret.setApprovals(approvals);

		return ret;
	}

	@Transactional
	public Document approve(String username, DocumentRequest request) {
		Document document = documentRepository.findById(request.getId())
				.orElseThrow(() -> new StyleException("해당 문서가 존재하지 않습니다."));

		Status curStat = document.getStatus();
		log.debug("approve ==========================" + curStat);

		if (curStat.equals(Status.approved)) {
			throw new StyleException("이미 결재된 문서입니다.");
		}
		else if (curStat.equals(Status.rejected)) {
			throw new StyleException("승인 거부된 문서입니다.");
		}

		String comment = request.getComment();

		AtomicReference<Approval> atomicReference = new AtomicReference<>();

		List<Approval> approvals = document.getApprovals();
		int count = approvals.size();
		int currIndex = 0;
		for (Approval a: approvals) {
			currIndex++;
			if (a.getApprover().getUsername().equals(username)) {
				atomicReference.set(a);
			}
			if (!a.isDone()) break;
			if (count == currIndex) {
				throw new StyleException("모든 결재자가 결재했지만 결재된 상태가 아닙니다.");
			}
		}

		Approval myApproval = atomicReference.get();
		if (myApproval == null) throw new StyleException("아직 결재순서가 아닙니다.");

		if (comment != null) myApproval.setComment(comment);
		myApproval.setDone(true);

		if (curStat.equals(Status.created)) {
			if (count == currIndex) {
				document.setStatus(Status.approved);
			} else {
				document.setStatus(Status.processed);
			}
		} else if (curStat.equals(Status.processed)) {
			if (count == currIndex) document.setStatus(Status.approved);
		} else {
			log.error("Cause of Unknown Error ==> " + curStat);
			throw new StyleException("Unknown Error");
		}

		return document;
	}

	@Transactional
	public Document reject(String username, DocumentRequest request) {
		Document document = documentRepository.findById(request.getId())
				.orElseThrow(() -> new StyleException("해당 문서가 존재하지 않습니다."));

		Status curStat = document.getStatus();

		if (curStat.equals(Status.approved)) {
			throw new StyleException("이미 결재된 문서입니다.");
		}
		else if (curStat.equals(Status.rejected)) {
			throw new StyleException("이미 승인 거부된 문서입니다.");
		}

		String comment = request.getComment();

		AtomicReference<Approval> atomicReference = new AtomicReference<>();

		List<Approval> approvals = document.getApprovals();
		int count = approvals.size();
		int currIndex = 0;
		for (Approval a: approvals) {
			currIndex++;
			if (a.getApprover().getUsername().equals(username)) {
				atomicReference.set(a);
			}
			if (!a.isDone()) break;
			if (count == currIndex) {
				throw new StyleException("모든 결재자가 결재했지만 결재된 상태가 아닙니다.");
			}
		}

		Approval myApproval = atomicReference.get();
		if (myApproval == null) throw new StyleException("아직 결재순서가 아닙니다.");

		if (comment != null) myApproval.setComment(comment);
		myApproval.setDone(true);

		if (curStat.equals(Status.created)) {
			document.setStatus(Status.rejected);
		} else if (curStat.equals(Status.processed)) {
			document.setStatus(Status.rejected);
		} else {
			log.error("Cause of Unknown Error ==> " + curStat);
			throw new StyleException("Unknown Error");
		}

		return document;
	}

	public List<Document> list(String username, String key) {
		switch (key) {
			// 내가 생성한 문서 중 결재 진행중인 문서
			case "OUTBOX" :
				return documentRepository.findByWriterAndStatusOrStatus(username, Status.created, Status.processed);
			// 내가 결재를 해야 할 문서
			case "INBOX" :
				return documentRepository.approvalRequired(username);
			// 내가 관여한 문서 중 결재가 완료(승인 또는 거절)된 문서
			case "ARCHIVE" :
				return documentRepository.participatedIn(username);
		}
	return null;
	}

}

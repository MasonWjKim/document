package com.example.style.service;

import com.example.style.domain.Member;
import com.example.style.repository.MemberRepository;
import com.example.style.request.MemberRequest;
import com.example.style.util.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {

	private final MemberRepository memberRepository;

	private final BCryptPasswordEncoder bCryptPasswordEncoder;

	public Member join(MemberRequest request) {
		Member member = Member.builder()
				.username(request.getUsername())
				.password(bCryptPasswordEncoder.encode(request.getPassword()))
				.build();

		return memberRepository.save(member);
	}

    public Member login(MemberRequest memberRequest) {
		Member member = memberRepository.findByUsername(memberRequest.getUsername());
		if(member != null && bCryptPasswordEncoder.matches(memberRequest.getPassword(), member.getPassword())) {
			CustomUserDetails userDetails = new CustomUserDetails(member.getUsername(), member.getPassword());
			Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(authentication);
			return member;
		}
		return null;
    }

}

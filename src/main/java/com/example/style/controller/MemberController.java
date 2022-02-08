package com.example.style.controller;

import com.example.style.domain.Member;
import com.example.style.request.MemberRequest;
import com.example.style.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor
@Controller
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String login(MemberRequest memberRequest) {
        Member member = memberService.login(memberRequest);
        if (member != null) {
            return "redirect:/";
        }
        return "redirect:/login";
    }

    @GetMapping("/join")
    public String join() {
        return "join";
    }

    @PostMapping("/join")
    public String join(MemberRequest request) {
        memberService.join(request);
        return "redirect:/";
    }
}

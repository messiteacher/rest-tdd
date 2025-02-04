package com.example.rest_tdd.domain.member.member.controller;

import com.example.rest_tdd.domain.member.member.dto.MemberDto;
import com.example.rest_tdd.domain.member.member.entity.Member;
import com.example.rest_tdd.domain.member.member.service.MemberService;
import com.example.rest_tdd.global.dto.RsData;
import com.example.rest_tdd.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class ApiV1MemberController {

    private final MemberService memberService;

    record JoinReqBody(String username, String password, String nickname) {}

    @PostMapping("/join")
    public RsData<MemberDto> join(@RequestBody JoinReqBody reqBody) {

        memberService.findByUsername(reqBody.username())
                .ifPresent(member -> {
                    throw new ServiceException("409-1", "이미 사용중인 아이디입니다.");
                });

        Member member = memberService.join(reqBody.username, reqBody.password, reqBody.nickname);

        return new RsData<>(
                "201-1",
                "회원 가입이 완료되었습니다.",
                new MemberDto(member)
        );
    }
}

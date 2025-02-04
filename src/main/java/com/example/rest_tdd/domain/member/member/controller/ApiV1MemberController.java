package com.example.rest_tdd.domain.member.member.controller;

import com.example.rest_tdd.global.dto.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class ApiV1MemberController {

    @PostMapping("/join")
    public RsData<Void> test() {
        return new RsData<>("201-1", "");
    }
}

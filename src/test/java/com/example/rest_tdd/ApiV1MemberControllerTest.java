package com.example.rest_tdd;

import com.example.rest_tdd.domain.member.member.controller.ApiV1MemberController;
import com.example.rest_tdd.domain.member.member.entity.Member;
import com.example.rest_tdd.domain.member.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ApiV1MemberControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("회원 가입")
    void join() throws Exception {

        ResultActions resultActions = mvc.perform(
                        post("/api/v1/members/join")
                                .content("""
                                        {
                                            "username": "userNew",
                                            "password": "1234",
                                            "nickname": "무명"
                                        }
                                        """.stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());

        Member member = memberService.findByUsername("userNew").get();

        assertThat(member.getNickname()).isEqualTo("무명");

        resultActions.andExpect(status().isCreated())
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(jsonPath("$.code").value("201-1"))
                .andExpect(jsonPath("$.msg").value("회원 가입이 완료되었습니다."))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.nickname").value("무명"))
                .andExpect(jsonPath("$.data.createdDate").exists())
                .andExpect(jsonPath("$.data.modifiedDate").exists());
    }

    @Test
    @DisplayName("회원 가입2 - username이 이미 존재하는 케이스")
    void login() throws Exception {

        ResultActions resultActions = mvc.perform(
                        post("/api/v1/members/join")
                                .content("""
                                        {
                                            "username": "user1",
                                            "password": "1234",
                                            "nickname": "무명"
                                        }
                                        """.stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());

        resultActions.andExpect(status().isConflict())
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(jsonPath("$.code").value("409-1"))
                .andExpect(jsonPath("$.msg").value("이미 사용중인 아이디입니다."));
    }

    @Test
    @DisplayName("로그인")
    void join2() throws Exception {

        String username = "user1";
        String password = "1234";

        ResultActions resultActions = mvc.perform(
                        post("/api/v1/members/login")
                                .content("""
                                        {
                                            "username": "%s",
                                            "password": "%s"
                                        }
                                        """.formatted(username, password)
                                        .stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());

        Member member = memberService.findByUsername(username).get();

        resultActions.andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("login"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%s님 환영합니다.".formatted(member.getNickname())))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.item.id").value(member.getId()))
                .andExpect(jsonPath("$.data.item.nickname").value(member.getNickname()))
                .andExpect(jsonPath("$.data.item.createdDate").value(member.getCreatedDate().toString()))
                .andExpect(jsonPath("$.data.item.modifiedDate").value(member.getModifiedDate().toString()))
                .andExpect(jsonPath("$.data.apiKey").exists());
    }
}

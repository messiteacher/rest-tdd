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
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    private void checkMember(ResultActions resultActions, Member member) throws Exception {

        resultActions.andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(member.getId()))
                .andExpect(jsonPath("$.data.nickname").value(member.getNickname()))
                .andExpect(jsonPath("$.data.createdDate").value(matchesPattern(member.getCreatedDate().toString().replaceAll("0+$", "") + ".*")))
                .andExpect(jsonPath("$.data.modifiedDate").value(matchesPattern(member.getModifiedDate().toString().replaceAll("0+$", "") + ".*")));
    }

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
                .andExpect(jsonPath("$.msg").value("회원 가입이 완료되었습니다."));

        checkMember(resultActions, member);
    }

    @Test
    @DisplayName("회원 가입2 - username이 이미 존재하는 케이스")
    void join2() throws Exception {

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

    private ResultActions loginRequest(String username, String password) throws Exception {

        return mvc.perform(
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
    }

    @Test
    @DisplayName("로그인 - 성공")
    void login1() throws Exception {

        String username = "user1";
        String password = "user11234";

        ResultActions resultActions = loginRequest(username, password);
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

    @Test
    @DisplayName("로그인 - 실패 - 비밀번호 틀림")
    void login2() throws Exception {

        String username = "user1";
        String password = "1234";

        ResultActions resultActions = loginRequest(username, password);

        resultActions.andExpect(status().isUnauthorized())
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("login"))
                .andExpect(jsonPath("$.code").value("401-2"))
                .andExpect(jsonPath("$.msg").value("비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("로그인 - 실패 - 존재하지 않는 username")
    void login3() throws Exception {

        String username = "";
        String password = "1234";

        ResultActions resultActions = loginRequest(username, password);

        resultActions.andExpect(status().isUnauthorized())
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("login"))
                .andExpect(jsonPath("$.code").value("401-1"))
                .andExpect(jsonPath("$.msg").value("잘못된 아이디입니다."));
    }

    private ResultActions meRequest(String apiKey) throws Exception {

        return mvc.perform(
                        get("/api/v1/members/me")
                                .header("Authorization", "Bearer " + apiKey)
                )
                .andDo(print());
    }

    @Test
    @DisplayName("내 정보 조회")
    void me1() throws Exception {

        String apiKey = "user1";

        ResultActions resultActions = mvc.perform(
                get("/api/v1/members/me")
                        .header("Authorization", "Bearer " + apiKey)
        ).andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("me"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.msg").value("내 정보 조회가 완료되었습니다."));

        Member member = memberService.findByApiKey(apiKey).get();
        checkMember(resultActions, member);
    }

    @Test
    @DisplayName("내 정보 조회 - 실패 - 잘못된 api key")
    void me2() throws Exception {

        String apiKey = "";

        ResultActions resultActions = meRequest(apiKey);

        resultActions.andExpect(status().isUnauthorized())
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("me"))
                .andExpect(jsonPath("$.code").value("401-1"))
                .andExpect(jsonPath("$.msg").value("잘못된 인증키입니다."));
    }
}

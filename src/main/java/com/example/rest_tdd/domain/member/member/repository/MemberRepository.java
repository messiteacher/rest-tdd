package com.example.rest_tdd.domain.member.member.repository;

import com.example.rest-tdd.domain.member.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>{
    Optional<Member> findByUsername(String username);

    Optional<Member> findByApiKey(String password2);
}
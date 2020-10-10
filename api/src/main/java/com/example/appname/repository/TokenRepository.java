package com.example.appname.repository;

import com.example.appname.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<Token, String> {
    Token findByToken(String token);

    void deleteByUserId(String userId);
}

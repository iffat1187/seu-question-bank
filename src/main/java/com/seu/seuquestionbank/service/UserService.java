package com.seu.seuquestionbank.service;

import com.seu.seuquestionbank.dto.RegisterDto;
import com.seu.seuquestionbank.model.User;

import java.util.Optional;

public interface UserService {
    User register(RegisterDto dto);
    Optional<User> findByEmail(String email);
    Optional<User> findById(String id);
}

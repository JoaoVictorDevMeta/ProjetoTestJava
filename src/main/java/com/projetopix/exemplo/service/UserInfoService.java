package com.projetopix.exemplo.service;

import com.projetopix.exemplo.entity.UserInfo;
import com.projetopix.exemplo.dto.ConsultaResponse;
import com.projetopix.exemplo.repository.UserInfoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Lazy;
import com.projetopix.exemplo.exception.UserAlreadyExistsException;

import java.util.Optional;

@Service
public class UserInfoService {

    private final UserInfoRepository repository;
    private final PasswordEncoder encoder;

    @Autowired
    public UserInfoService(UserInfoRepository repository, @Lazy PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public String addUser(UserInfo userInfo) {
        if (repository.findByEmail(userInfo.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User with this email already exists!");
        }
        userInfo.setPassword(encoder.encode(userInfo.getPassword()));
        userInfo.setScore(5.0);
        userInfo.setSaldo(0.0);
        userInfo.setSeloVerificado("nao");
        repository.save(userInfo);
        return "User added successfully";
    }

    public Optional<UserInfo> findByCpf(String cpf) {
        return repository.findByCpf(cpf);
    }

    public ConsultaResponse consultarUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        var user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        ConsultaResponse resp = new ConsultaResponse();
        resp.setSaldo(user.getSaldo());
        resp.setScore(user.getScore());
        resp.setSeloVerificado(user.getSeloVerificado());
        resp.setBloqueado(user.getScore() != null && user.getScore() < 2.5);
        return resp;
    }

    // Adicione métodos para atualizar saldo, score, bloqueio, etc.
}
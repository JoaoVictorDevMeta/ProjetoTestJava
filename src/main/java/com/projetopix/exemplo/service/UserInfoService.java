package com.projetopix.exemplo.service;

import com.projetopix.exemplo.entity.UserInfo;
import com.projetopix.exemplo.repository.TransactionRepository;
import com.projetopix.exemplo.exception.UserAlreadyExistsException;
import com.projetopix.exemplo.dto.ConsultaResponse;
import com.projetopix.exemplo.entity.Transaction;
import com.projetopix.exemplo.repository.UserInfoRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserInfoService {

    private final UserInfoRepository repository;
    private final PasswordEncoder encoder;
    private final TransactionRepository transactionRepository;

    public String addUser(UserInfo userInfo) {
        if (repository.findByCpf(userInfo.getCpf()).isPresent()) {
            throw new UserAlreadyExistsException("Usuário ja esta cadastrado!");
        }
        if (userInfo.getDataNascimento() == null || userInfo.getDataNascimento().isBlank()) {
            throw new IllegalArgumentException("Data de nascimento é obrigatória!");
        }
        userInfo.setPassword(encoder.encode(userInfo.getPassword()));
        userInfo.setRoles("ROLE_USER");
        userInfo.setScore(5.0);
        userInfo.setSaldo(0.0);
        userInfo.setSeloVerificado("nao");
        userInfo.setBloqueado(false);
        repository.save(userInfo);
        return "User added successfully";
    }

    public Optional<UserInfo> findByCpf(String cpf) {
        return repository.findByCpf(cpf);
    }

    public ConsultaResponse consultarUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String cpf = auth.getName(); // Agora o username é o CPF
        var user = repository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        ConsultaResponse resp = new ConsultaResponse();
        resp.setSaldo(user.getSaldo());
        resp.setScore(user.getScore());
        resp.setSeloVerificado(user.getSeloVerificado());
        resp.setBloqueado(user.getScore() != null && user.getScore() < 2.5);
        return resp;
    }

    public List<Transaction> consultarExtratoUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String cpf = auth.getName(); // username é o CPF
        UserInfo user = repository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Busca transações onde o usuário é remetente ou destinatário
        return transactionRepository.findByRemetenteOrDestinatario(user, user);
    }
}
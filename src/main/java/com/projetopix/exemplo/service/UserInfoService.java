package com.projetopix.exemplo.service;

import com.projetopix.exemplo.entity.UserInfo;
import com.projetopix.exemplo.repository.DenunciaRepository;
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
    private final DenunciaRepository denunciaRepository;

    public String addUser(UserInfo userInfo) {
        if (repository.findByCpf(userInfo.getCpf()).isPresent()) {
            throw new UserAlreadyExistsException("Usuário ja esta cadastrado!");
        }
        if (userInfo.getDataNascimento() == null || userInfo.getDataNascimento().isBlank()) {
            throw new IllegalArgumentException("Data de nascimento é obrigatória!");
        }
        userInfo.setPassword(encoder.encode(userInfo.getPassword()));
        userInfo.setName(userInfo.getName().toUpperCase());
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
        String cpf = auth.getName();
        var user = repository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        ConsultaResponse resp = new ConsultaResponse();
        resp.setName(user.getName());
        resp.setCpf(user.getCpf());
        resp.setTipoConta(user.getTipoConta());
        resp.setSaldo(user.getSaldo());
        resp.setRoles(user.getRoles());
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

    public String pedirSeloVerificado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String cpf = auth.getName();
        UserInfo user = repository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if ("sim".equalsIgnoreCase(user.getSeloVerificado())) {
            return "Usuário já possui selo verificado.";
        }

        long qtdTransacoes = transactionRepository.findByRemetenteOrDestinatario(user, user).size();
        if (qtdTransacoes < 20) {
            return "Usuário precisa de pelo menos 20 transações para solicitar o selo.";
        }

        // Use o repository para buscar denúncias aprovadas
        boolean temDenunciaAprovada = !denunciaRepository
                .findByDenunciadoCpfAndStatus(user.getCpf(), "aprovada")
                .isEmpty();

        if (temDenunciaAprovada) {
            return "Usuário não pode solicitar selo pois possui denúncia aprovada.";
        }

        user.setSeloVerificado("sim");
        repository.save(user);
        return "Selo verificado concedido!";
    }

    public ConsultaResponse consultarPorCpf(String cpf) {
        var user = repository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        ConsultaResponse resp = new ConsultaResponse();
        resp.setName(user.getName());
        resp.setCpf(user.getCpf());
        resp.setTipoConta(user.getTipoConta());
        resp.setRoles(user.getRoles());
        resp.setSaldo(user.getSaldo());
        resp.setScore(user.getScore());
        resp.setSeloVerificado(user.getSeloVerificado());
        resp.setBloqueado(user.getScore() != null && user.getScore() < 2.5);
        return resp;
    }
}
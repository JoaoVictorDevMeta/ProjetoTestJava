package com.projetopix.exemplo.service;

import com.projetopix.exemplo.entity.UserInfo;
import com.projetopix.exemplo.repository.DenunciaRepository;
import com.projetopix.exemplo.repository.TransactionRepository;
import com.projetopix.exemplo.exception.UserAlreadyExistsException;
import com.projetopix.exemplo.dto.ConsultaResponse;
import com.projetopix.exemplo.dto.NotificationResponse;
import com.projetopix.exemplo.dto.EmergenciaConta;
import com.projetopix.exemplo.entity.Transaction;
import com.projetopix.exemplo.repository.UserInfoRepository;
import com.projetopix.exemplo.entity.Notification;
import com.projetopix.exemplo.repository.NotificationRepository;

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
    private final NotificationRepository notificationRepository;
    private final JwtService jwtService;

    public PasswordEncoder getEncoder() {
        return encoder;
    }

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

        // notificar o usuário com um bem-vindo
        Notification notification = Notification.builder()
                .titulo("Bem-vindo ao Jubran Bank!")
                .mensagem("Sua conta foi criada com sucesso. Aproveite nossos serviços!")
                .dataCriacao(java.time.LocalDateTime.now().toString())
                .lida(false)
                .usuario(userInfo)
                .build();
        notificationRepository.save(notification);

        return "User added successfully";
    }

    public Optional<UserInfo> findByCpf(String cpf) {
        return repository.findByCpf(cpf);
    }

    public ConsultaResponse consultarUsuarioAutenticado(String token) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isEmergencia = jwtService.isEmergencia(token);
        String cpf = auth.getName();
        UserInfo user = repository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        ConsultaResponse resp = new ConsultaResponse();
        resp.setName(user.getName());
        resp.setCpf(user.getCpf());
        resp.setTipoConta(user.getTipoConta());
        // deve ser alterado de acordo com a emergencia
        if (isEmergencia) {
            resp.setSaldo(user.getContaEmergenciaSaldo() != null ? user.getContaEmergenciaSaldo() : 0.0);
        } else {
            resp.setSaldo(user.getSaldo());
        }

        // ==================
        // simulando selo simples
        long qtdTransacoes = transactionRepository.findByRemetenteOrDestinatario(user, user).size();
        boolean temDenunciaAprovada = !denunciaRepository
                .findByDenunciadoCpfAndStatus(user.getCpf(), "aprovada")
                .isEmpty();

        if (qtdTransacoes >= 5 && !temDenunciaAprovada) {
            user.setSeloVerificado("sim");
        } else {
            user.setSeloVerificado("nao");
        }

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
        if (qtdTransacoes < 5) {
            throw new RuntimeException("Usuário precisa de pelo menos 5 transações para solicitar o selo.");
        }

        // Use o repository para buscar denúncias aprovadas
        boolean temDenunciaAprovada = !denunciaRepository
                .findByDenunciadoCpfAndStatus(user.getCpf(), "aprovada")
                .isEmpty();

        if (temDenunciaAprovada) {
            throw new RuntimeException("Usuário não pode solicitar selo pois possui denúncia aprovada.");
        }

        return "Selo verificado enviado para aprovação";
    }

    public ConsultaResponse consultarPorCpf(String cpf) {
        UserInfo user = repository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        ConsultaResponse resp = new ConsultaResponse();

        long qtdTransacoes = transactionRepository.findByRemetenteOrDestinatario(user, user).size();
        boolean temDenunciaAprovada = !denunciaRepository
                .findByDenunciadoCpfAndStatus(user.getCpf(), "aprovada")
                .isEmpty();

        if (qtdTransacoes >= 5 && !temDenunciaAprovada) {
            user.setSeloVerificado("sim");
        } else {
            user.setSeloVerificado("nao");
        }

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

    public List<NotificationResponse> consultarNotificacoesUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String cpf = auth.getName();
        UserInfo user = repository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<Notification> notifications = notificationRepository.findByUsuario(user);
        return notifications.stream().map(notification -> {
            NotificationResponse resp = new NotificationResponse();
            resp.setTitulo(notification.getTitulo());
            resp.setMensagem(notification.getMensagem());
            resp.setDataCriacao(notification.getDataCriacao());
            resp.setLida(notification.getLida());
            return resp;
        }).toList();
    }

    public String definirContaEmergencia(EmergenciaConta request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usuarioCpf = auth.getName();

        UserInfo user = repository.findByCpf(usuarioCpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (request.getPercentage() < 0 || request.getPercentage() > 100) {
            throw new RuntimeException("Percentual deve ser entre 0 e 100.");
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (encoder.matches(request.getPassword(), user.getPassword())) {
                throw new RuntimeException("A senha de emergência não pode ser igual à senha principal.");
            }
            user.setSegundaSenha(encoder.encode(request.getPassword()));
        } else {
            user.setSegundaSenha(null);
        }

        user.setContaEmergenciaPercentual(
                request.getPercentage() != null ? request.getPercentage().doubleValue() : null);
        user.setSegundaSenha(
                request.getPassword() != null && !request.getPassword().isBlank()
                        ? encoder.encode(request.getPassword())
                        : null);
        user.setContaEmergenciaSaldo(
                user.getSaldo()
                        * (user.getPercentualSeguranca() != null ? user.getPercentualSeguranca() / 100.0 : 0.0));

        repository.save(user);

        return "Conta de emergência definida com sucesso!";
    }

    public void saveUser(UserInfo user) {
        repository.save(user);
    }
}
package com.projetopix.exemplo.service;

import com.projetopix.exemplo.dto.DenunciaRequest;
import com.projetopix.exemplo.entity.Denuncia;
import com.projetopix.exemplo.entity.Notification;
import com.projetopix.exemplo.entity.UserInfo;
import com.projetopix.exemplo.repository.DenunciaRepository;
import com.projetopix.exemplo.repository.NotificationRepository;
import com.projetopix.exemplo.repository.UserInfoRepository;
import com.projetopix.exemplo.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class DenunciaService {

    private final DenunciaRepository denunciaRepository;
    private final UserInfoRepository userInfoRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationRepository notificationRepository;

    public void denunciar(DenunciaRequest request) {
        // Pega o CPF do usuário autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String cpfAutenticado = auth.getName();

        // Busca o denunciante pelo CPF autenticado
        UserInfo denunciante = userInfoRepository.findByCpf(cpfAutenticado)
                .orElseThrow(() -> new RuntimeException("Denunciante não encontrado!"));

        // Busca o denunciado normalmente pelo request
        UserInfo denunciado = userInfoRepository.findByCpf(request.getCpfDenunciado())
                .orElseThrow(() -> new RuntimeException("Denunciado não encontrado!"));

        if (request.getCodigoTransacao() == null || request.getCodigoTransacao().isBlank()) {
            throw new RuntimeException("Código da transação é obrigatório!");
        }
        if (request.getMotivo() == null || request.getMotivo().isBlank()) {
            throw new RuntimeException("Motivo da denúncia é obrigatório!");
        }

        boolean transacaoExiste = transactionRepository.findByCodigoTransacao(request.getCodigoTransacao()).isPresent();
        if (!transacaoExiste) {
            throw new RuntimeException("Código de transação inválido!");
        }

        Denuncia denuncia = Denuncia.builder()
                .codigoTransacao(request.getCodigoTransacao())
                .motivo(request.getMotivo())
                .detalhes(request.getDetalhes())
                .dataHora(new Date())
                .denunciante(denunciante)
                .denunciado(denunciado)
                .status("pendente")
                .build();

        denunciaRepository.save(denuncia);

        Notification notification = Notification.builder()
                .titulo("Denúncia registrada")
                .mensagem("sua denúncia para " + denunciado.getName()
                        + " foi registrada com sucesso. A equipe Jubran Bank irá analisar em breve.")
                .dataCriacao(java.time.LocalDateTime.now().toString())
                .lida(false)
                .usuario(denunciante)
                .build();
        notificationRepository.save(notification);
    }

    public void atualizarStatusDenuncia(Long denunciaId, String status) {
        Denuncia denuncia = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new RuntimeException("Denúncia não encontrada!"));

        // Apenas aceita "aprovada" ou "invalidada"
        if (!status.equalsIgnoreCase("aprovada") && !status.equalsIgnoreCase("invalidada")) {
            throw new RuntimeException("Status inválido! Use 'aprovada' ou 'invalidada'.");
        }

        denuncia.setStatus(status.toLowerCase());
        denunciaRepository.save(denuncia);

        if (status.equalsIgnoreCase("aprovada")) {
            // Reduz 0.5 do score do denunciado
            UserInfo denunciado = denuncia.getDenunciado();
            double novoScore = denunciado.getScore() - 0.5;
            if (novoScore < 0.0)
                novoScore = 0.0;
            denunciado.setScore(novoScore);

            if (novoScore < 2.5) {
                Notification notification = Notification.builder()
                        .titulo("CONTA BLOQUEADA")
                        .mensagem("Devido a múltiplas denúncias, sua conta foi bloqueada. "
                                + "Entre em contato com o suporte para mais informações.")
                        .dataCriacao(java.time.LocalDateTime.now().toString())
                        .lida(false)
                        .usuario(denunciado)
                        .build();
                notificationRepository.save(notification);
                denunciado.setBloqueado(true);
            } else {
                Notification notification = Notification.builder()
                        .titulo("Denúncia aprovada")
                        .mensagem("Sua denúncia contra " + denunciado.getName()
                                + " foi aprovada. O Jubran Bank está tomando as medidas necessárias.")
                        .dataCriacao(java.time.LocalDateTime.now().toString())
                        .lida(false)
                        .usuario(denuncia.getDenunciante())
                        .build();
                notificationRepository.save(notification);
            }
        
            userInfoRepository.save(denunciado);
        } else if (status.equalsIgnoreCase("invalidada")) {
            // Reduz 0.1 do score do denunciante
            UserInfo denunciante = denuncia.getDenunciante();
            double novoScore = denunciante.getScore() - 0.1;
            if (novoScore < 0.0)
                novoScore = 0.0;
            denunciante.setScore(novoScore);

             if (novoScore < 2.5) {
                Notification notification = Notification.builder()
                        .titulo("CONTA BLOQUEADA")
                        .mensagem("Devido a múltiplas denúncias, sua conta foi bloqueada. "
                                + "Entre em contato com o suporte para mais informações.")
                        .dataCriacao(java.time.LocalDateTime.now().toString())
                        .lida(false)
                        .usuario(denunciante)
                        .build();
                notificationRepository.save(notification);
                denunciante.setBloqueado(true);
            } else {
                Notification notification = Notification.builder()
                        .titulo("Denúncia invalidada")
                        .mensagem("Sua denúncia contra " + denuncia.getDenunciado().getName()
                                + " foi invalidada. O Jubran Bank não encontrou evidências suficientes.")
                        .dataCriacao(java.time.LocalDateTime.now().toString())
                        .lida(false)
                        .usuario(denunciante)
                        .build();
                notificationRepository.save(notification);
            }
            userInfoRepository.save(denunciante);
        }
    }

    public void aprovarSeloVerificado(String cpf) {
        UserInfo user = userInfoRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));

        if ("sim".equalsIgnoreCase(user.getSeloVerificado())) {
            throw new RuntimeException("Usuário já possui selo verificado.");
        }

        user.setSeloVerificado("sim");
        userInfoRepository.save(user);
    }

    public List<DenunciaRequest> consultarDenunciasUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String cpf = auth.getName();

        // Busca denúncias feitas pelo usuário
        List<Denuncia> feitas = denunciaRepository.findByDenuncianteCpf(cpf);
        // Busca denúncias recebidas pelo usuário
        List<Denuncia> recebidas = denunciaRepository.findByDenunciadoCpf(cpf);

        List<DenunciaRequest> resultado = new ArrayList<>();

        for (Denuncia d : feitas) {
            DenunciaRequest req = new DenunciaRequest();
            req.setCpfDenunciado(d.getDenunciado().getCpf());
            req.setCodigoTransacao(d.getCodigoTransacao());
            req.setMotivo(d.getMotivo());
            req.setDetalhes(d.getDetalhes());
            resultado.add(req);
        }
        for (Denuncia d : recebidas) {
            DenunciaRequest req = new DenunciaRequest();
            req.setCpfDenunciado(d.getDenunciado().getCpf());
            req.setCodigoTransacao(d.getCodigoTransacao());
            req.setMotivo(d.getMotivo());
            req.setDetalhes(d.getDetalhes());
            resultado.add(req);
        }
        return resultado;
    }
}
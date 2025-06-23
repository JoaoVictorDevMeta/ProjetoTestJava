package com.projetopix.exemplo.service;

import com.projetopix.exemplo.dto.DenunciaRequest;
import com.projetopix.exemplo.entity.Denuncia;
import com.projetopix.exemplo.entity.UserInfo;
import com.projetopix.exemplo.repository.DenunciaRepository;
import com.projetopix.exemplo.repository.UserInfoRepository;
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

        // FALTA NOTIFICAÇÃO
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
            // Reduz 0.3 do score do denunciado
            UserInfo denunciado = denuncia.getDenunciado();
            double novoScore = denunciado.getScore() - 0.3;
            if (novoScore < 0.0)
                novoScore = 0.0;
            denunciado.setScore(novoScore);
            denunciado.setBloqueado(novoScore < 2.5);
            userInfoRepository.save(denunciado);
        } else if (status.equalsIgnoreCase("invalidada")) {
            // Reduz 0.1 do score do denunciante
            UserInfo denunciante = denuncia.getDenunciante();
            double novoScore = denunciante.getScore() - 0.1;
            if (novoScore < 0.0)
                novoScore = 0.0;
            denunciante.setScore(novoScore);
            denunciante.setBloqueado(novoScore < 2.5);
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
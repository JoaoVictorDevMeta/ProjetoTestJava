package com.projetopix.exemplo.service;

import com.projetopix.exemplo.dto.DenunciaRequest;
import com.projetopix.exemplo.entity.Denuncia;
import com.projetopix.exemplo.entity.UserInfo;
import com.projetopix.exemplo.repository.DenunciaRepository;
import com.projetopix.exemplo.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class DenunciaService {

    private final DenunciaRepository denunciaRepository;
    private final UserInfoRepository userInfoRepository;

    public void denunciar(DenunciaRequest request) {
        UserInfo denunciante = userInfoRepository.findByCpf(request.getCpfDenunciante())
                .orElseThrow(() -> new RuntimeException("Denunciante não encontrado!"));
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

        // Atualiza o score do denunciado
        double novoScore = denunciado.getScore() - 0.5;
        if (novoScore < 0.0)
            novoScore = 0.0;
        denunciado.setScore(novoScore);

        // Bloqueia se score < 2.5
        denunciado.setBloqueado(novoScore < 2.5);

        userInfoRepository.save(denunciado);

        // FALTA NOTIFICAÇÃO
    }

    // Métodos para justificar, aprovar, reprovar, etc.
}
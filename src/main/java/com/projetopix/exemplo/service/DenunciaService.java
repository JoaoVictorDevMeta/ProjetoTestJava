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
            .orElseThrow(() -> new RuntimeException("Denunciante não encontrado"));
        UserInfo denunciado = userInfoRepository.findByCpf(request.getCpfDenunciado())
            .orElseThrow(() -> new RuntimeException("Denunciado não encontrado"));

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

        // Atualize score, notificações, etc.
    }

    // Métodos para justificar, aprovar, reprovar, etc.
}
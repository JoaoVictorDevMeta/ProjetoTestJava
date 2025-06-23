package com.projetopix.exemplo.service;

import com.projetopix.exemplo.dto.DepositoRequest;
import com.projetopix.exemplo.dto.TransferenciaRequest;
import com.projetopix.exemplo.entity.Transaction;
import com.projetopix.exemplo.entity.UserInfo;
import com.projetopix.exemplo.repository.TransactionRepository;
import com.projetopix.exemplo.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserInfoRepository userInfoRepository;

    public void depositar(DepositoRequest request) {
        UserInfo origem = userInfoRepository.findByCpf(request.getCpfOrigem())
            .orElseThrow(() -> new RuntimeException("Usuário origem não encontrado"));
        UserInfo destino = userInfoRepository.findByCpf(request.getChaveDestino())
            .orElseThrow(() -> new RuntimeException("Usuário destino não encontrado"));

        // Validações de score, saldo, etc.
        if (destino.getScore() < 2.5) throw new RuntimeException("Conta destino bloqueada para depósitos.");

        destino.setSaldo(destino.getSaldo() + request.getValor());
        userInfoRepository.save(destino);

        Transaction tx = Transaction.builder()
            .codigoTransacao(gerarCodigoTransacao())
            .descricao("Depósito de " + origem.getName())
            .valor(request.getValor())
            .dataHora(new Date())
            .remetente(origem)
            .destinatario(destino)
            .build();
        transactionRepository.save(tx);

        // Atualize score, notificações, etc.
    }

    public void transferir(TransferenciaRequest request) {
        // Implemente lógica semelhante ao depósito, com validações e taxas
    }

    private String gerarCodigoTransacao() {
        return "TX-" + String.format("%06d", (int)(Math.random() * 1000000));
    }
}
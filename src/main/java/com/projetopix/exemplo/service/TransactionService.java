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
        UserInfo destino = userInfoRepository.findByCpf(request.getCpfDestino())
                .orElseThrow(() -> new RuntimeException("Usuário destino não encontrado"));

        if (destino.getScore() < 2.5)
            throw new RuntimeException("Conta destino bloqueada para depósitos.");

        // definindo saldo e score
        destino.setSaldo(destino.getSaldo() + request.getValor());
        double novoScore = destino.getScore() + 0.1;
        if (novoScore > 5.0)
            novoScore = 5.0;
        destino.setScore(novoScore);
        // salvando no usuário
        userInfoRepository.save(destino);

        // FALTA NOTIFICAÇÃO

        // registrando transação
        Transaction tx = Transaction.builder()
                .codigoTransacao(gerarCodigoTransacao())
                .descricao("Depósito")
                .valor(request.getValor())
                .dataHora(new Date())
                .remetente(null) // Origem externa (apenas para teste)
                .destinatario(destino)
                .build();
        transactionRepository.save(tx);
    }

    public void transferir(TransferenciaRequest request) {
        UserInfo remetente = userInfoRepository.findByCpf(request.getCpfRemetente())
                .orElseThrow(() -> new RuntimeException("Usuário remetente não encontrado"));
        UserInfo destinatario = userInfoRepository.findByCpf(request.getCpfDestino())
                .orElseThrow(() -> new RuntimeException("Usuário destinatário não encontrado"));

        if (remetente.getScore() < 2.5 || (remetente.getBloqueado() != null && remetente.getBloqueado()))
            throw new RuntimeException("Conta remetente bloqueada para transferências.");
        if (destinatario.getScore() < 2.5 || (destinatario.getBloqueado() != null && destinatario.getBloqueado()))
            throw new RuntimeException("Conta destinatário bloqueada para receber transferências.");

        // Cálculo da taxa de 1% =================================
        // PODE SER OPCIONAL, APENAS COLOQUEI POR QUE ESTAVA NO OUTRO CODIGO
        double taxa = request.getValor() * 0.01; // <-- TAXA DE 1%
        double valorTotal = request.getValor() + taxa;
        // ========================================================

        // Verifica saldo suficiente (incluindo a taxa)
        if (remetente.getSaldo() < valorTotal)
            throw new RuntimeException("Saldo insuficiente para transferência (incluindo taxa de 1%).");

        // Realiza a transferência
        remetente.setSaldo(remetente.getSaldo() - valorTotal); 
        destinatario.setSaldo(destinatario.getSaldo() + request.getValor()); // Destinatário recebe valor integral

        // Atualiza score
        double novoScoreDest = destinatario.getScore() + 0.1;
        if (novoScoreDest > 5.0)
            novoScoreDest = 5.0;
        destinatario.setScore(novoScoreDest);

        userInfoRepository.save(remetente);
        userInfoRepository.save(destinatario);

        // Registra transação (deixe explícito na descrição o valor da taxa)
        Transaction tx = Transaction.builder()
                .codigoTransacao(gerarCodigoTransacao())
                .descricao("Transferência (taxa de 1%: " + String.format("%.2f", taxa) + ")")
                .valor(request.getValor())
                .dataHora(new Date())
                .remetente(remetente)
                .destinatario(destinatario)
                .build();
        transactionRepository.save(tx);
    }

    private String gerarCodigoTransacao() {
        return "TX-" + String.format("%06d", (int) (Math.random() * 1000000));
    }
}
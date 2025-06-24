package com.projetopix.exemplo.repository;

import com.projetopix.exemplo.entity.Transaction;
import com.projetopix.exemplo.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByRemetenteOrDestinatario(UserInfo remetente, UserInfo destinatario);
    Optional<Transaction> findByCodigoTransacao(String codigoTransacao);
}
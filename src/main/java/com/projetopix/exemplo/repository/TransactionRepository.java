package com.projetopix.exemplo.repository;

import com.projetopix.exemplo.entity.Transaction;
import com.projetopix.exemplo.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByRemetenteOrDestinatario(UserInfo remetente, UserInfo destinatario);
}
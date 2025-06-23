package com.projetopix.exemplo.repository;

import com.projetopix.exemplo.entity.Denuncia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DenunciaRepository extends JpaRepository<Denuncia, Long> {
    List<Denuncia> findByDenunciadoCpf(String cpf);
    List<Denuncia> findByDenuncianteCpf(String cpf);
    List<Denuncia> findByCodigoTransacao(String codigoTransacao);
}
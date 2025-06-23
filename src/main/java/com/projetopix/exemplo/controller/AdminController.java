package com.projetopix.exemplo.controller;

import com.projetopix.exemplo.entity.Transaction;
import com.projetopix.exemplo.entity.Denuncia;
import com.projetopix.exemplo.service.DenunciaService;
import com.projetopix.exemplo.repository.TransactionRepository;
import com.projetopix.exemplo.repository.DenunciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final TransactionRepository transactionRepository;
    private final DenunciaRepository denunciaRepository;
    private final DenunciaService denunciaService;

    // Retorna todos os extratos (todas as transações)
    @GetMapping("/todos-extratos")
    public List<Transaction> todosExtratos() {
        return transactionRepository.findAll();
    }

    // Retorna todas as denúncias
    @GetMapping("/todas-denuncias")
    public List<Denuncia> todasDenuncias() {
        return denunciaRepository.findAll();
    }

    // Atualiza o status da denúncia (aprovar ou invalidar)
    @PostMapping("/atualizar-denuncia")
    public String atualizarDenuncia(@RequestParam Long denunciaId, @RequestParam String status) {
        // status esperado: "aprovada" ou "invalidada"
        denunciaService.atualizarStatusDenuncia(denunciaId, status);
        return "Status da denúncia atualizado para: " + status;
    }

    //aprovar selo verificado
    @PostMapping("/aprovar-selo-verificado")
    public String aprovarSeloVerificado(@RequestParam String cpf) {
        denunciaService.aprovarSeloVerificado(cpf);
        return "Selo verificado aprovado para o usuário com CPF: " + cpf;
    }
}
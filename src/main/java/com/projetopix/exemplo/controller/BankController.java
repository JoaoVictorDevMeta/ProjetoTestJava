package com.projetopix.exemplo.controller;

import com.projetopix.exemplo.dto.DepositoRequest;
import com.projetopix.exemplo.dto.TransferenciaRequest;
import com.projetopix.exemplo.dto.DenunciaRequest;
import com.projetopix.exemplo.service.TransactionService;
import com.projetopix.exemplo.service.DenunciaService;
import com.projetopix.exemplo.dto.ConsultaResponse;
import com.projetopix.exemplo.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bank")
@RequiredArgsConstructor
public class BankController {

    private final TransactionService transactionService;
    private final DenunciaService denunciaService;
    private final UserInfoService userInfoService;

    @GetMapping("/status")
    public String status() {
        return "Servidor do banco está funcionando corretamente!";
    }

    //consultar saldo, score, selo verificado, bloqueio, etc. do usuário
    @GetMapping("/consultar")
    public ConsultaResponse consultar() {
        return userInfoService.consultarUsuarioAutenticado();
    }

    @PostMapping("/depositar")
    public String depositar(@RequestBody DepositoRequest request) {
        transactionService.depositar(request);
        return "Depósito realizado com sucesso!";
    }

    @PostMapping("/transferir")
    public String transferir(@RequestBody TransferenciaRequest request) {
        transactionService.transferir(request);
        return "Transferência realizada com sucesso!";
    }

    @PostMapping("/denunciar")
    public String denunciar(@RequestBody DenunciaRequest request) {
        denunciaService.denunciar(request);
        return "Denúncia registrada com sucesso!";
    }
}
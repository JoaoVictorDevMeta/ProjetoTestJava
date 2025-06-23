package com.projetopix.exemplo.dto;

import lombok.Data;

@Data
public class ConsultaResponse {
    private String name;
    private String cpf;
    private String tipoConta;
    private String roles;
    private Double saldo;
    private Double score;
    private String seloVerificado;
    private boolean bloqueado;
}
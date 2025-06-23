package com.projetopix.exemplo.dto;

import lombok.Data;

@Data
public class ConsultaResponse {
    private Double saldo;
    private Double score;
    private String seloVerificado;
    private boolean bloqueado;
}
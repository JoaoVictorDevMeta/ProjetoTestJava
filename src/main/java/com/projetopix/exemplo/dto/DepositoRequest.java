package com.projetopix.exemplo.dto;

import lombok.Data;

@Data
public class DepositoRequest {
    private String cpfDestino;
    private Double valor;
}
package com.projetopix.exemplo.dto;

import lombok.Data;

@Data
public class DepositoRequest {
    private String cpfOrigem;
    private String chaveDestino;
    private Double valor;
}
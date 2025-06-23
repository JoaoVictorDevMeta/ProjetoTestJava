package com.projetopix.exemplo.dto;

import lombok.Data;

@Data
public class TransferenciaRequest {
    private String cpfRemetente;
    private String cpfDestino;
    private Double valor;
}
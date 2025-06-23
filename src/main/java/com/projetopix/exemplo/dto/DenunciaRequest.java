package com.projetopix.exemplo.dto;

import lombok.Data;

@Data
public class DenunciaRequest {
    private String codigoTransacao;
    private String motivo;
    private String detalhes;
    private String cpfDenunciante;
    private String cpfDenunciado;
}
package com.projetopix.exemplo.dto;

import lombok.Data;

@Data
public class NotificationResponse {
    private String titulo;
    private String mensagem;
    private String dataCriacao;
    private Boolean lida;
}
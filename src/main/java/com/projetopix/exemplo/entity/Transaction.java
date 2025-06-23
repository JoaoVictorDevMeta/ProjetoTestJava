package com.projetopix.exemplo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigoTransacao;
    private String descricao;
    private Double valor;
    private Date dataHora;

    @ManyToOne
    private UserInfo remetente;

    @ManyToOne
    private UserInfo destinatario;
}
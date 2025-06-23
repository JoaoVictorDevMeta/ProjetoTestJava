package com.projetopix.exemplo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Denuncia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigoTransacao;
    private String motivo;
    private String detalhes;
    private Date dataHora;

    @ManyToOne
    private UserInfo denunciante;

    @ManyToOne
    private UserInfo denunciado;

    private String status; // pendente, justificada, aprovada, reprovada
}
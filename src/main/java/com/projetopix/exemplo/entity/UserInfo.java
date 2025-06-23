package com.projetopix.exemplo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    
    @Column(unique = true)
    private String email;
    private String password;
    private String roles;

    @Column(unique = true)
    private String cpf;
    private String tipoConta;
    private String telefone;
    private String estado;
    private String dataNascimento;
    private Double saldo;
    private Double score;
    private String segundaSenha;
    private String chaveDeposito;
    private String seloVerificado;
    private Double percentualSeguranca;
}
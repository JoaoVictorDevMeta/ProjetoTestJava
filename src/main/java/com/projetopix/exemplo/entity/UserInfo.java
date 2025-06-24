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

    @Column(unique = true, nullable = false)
    private String cpf;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String seloVerificado;

    @Column(nullable = false)
    private String dataNascimento;
    private String name;
    private String roles;
    private Boolean bloqueado;

    private String tipoConta;
    private String telefone;
    private String estado;
    private Double saldo;
    private Double score;
    private String segundaSenha;
    private Double contaEmergenciaSaldo;
    private String chaveDeposito;
    private Double percentualSeguranca;

    public void setBloqueado(Boolean bloqueado) {
        this.bloqueado = bloqueado;
    }

    public Boolean getBloqueado() {
        return bloqueado;
    }

    public void setContaEmergenciaPercentual(Double contaEmergenciaPercentual) {
        this.percentualSeguranca = contaEmergenciaPercentual;
    }
}
package com.projetopix.exemplo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String mensagem;

    @Column(nullable = false)
    private String dataCriacao;

    private Boolean lida;

    //relacionamento com UserInfo
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserInfo usuario;

    public void setLida(Boolean lida) {
        this.lida = lida;
    }

    public Boolean getLida() {
        return lida;
    }
}
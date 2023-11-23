package br.ufsm.csi.tapw.pilacoin.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "usuario")
public class Usuario {
    private Long id;
    @Column(name = "chave_publica")
    private byte[] chavePublica;
    @Id
    @Column(name = "nome")
    private String nome;
}

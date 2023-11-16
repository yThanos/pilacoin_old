package br.ufsm.csi.tapw.pilacoin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @Column(name = "chave_publica")
    private byte[] chavePublciaUsuario;

    @Id
    @Column(name = "nome")
    private String nome;
}

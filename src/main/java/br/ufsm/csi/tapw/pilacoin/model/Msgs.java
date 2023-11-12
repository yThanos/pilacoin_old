package br.ufsm.csi.tapw.pilacoin.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder(alphabetic = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "mensagens")
public class Msgs {
    @Id
    @Column(name = "id_msg")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "erro")
    private String erro;
    @Column(name = "msg")
    private String msg;
    @Column(name = "usuario")
    private String nomeUsuario;
    @Column(name = "nonce")
    private String nonce;
    @Column(name = "queue")
    private String queue;
    @Column(name = "lida")
    private boolean lida;
}

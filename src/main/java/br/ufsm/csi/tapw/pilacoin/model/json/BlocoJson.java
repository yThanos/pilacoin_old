package br.ufsm.csi.tapw.pilacoin.model.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder(alphabetic = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "bloco")
public class BlocoJson {
    @Id
    @Column(name = "numero_bloco")
    private int numeroBloco;
    @Column(name = "nonce_anteior")
    private String nonceBlocoAnterior;
    @Column(name = "nonce")
    private String nonce;
    private String nomeUsuarioMinerador;
    private byte[] chaveUsuarioMinerador;
    private List<Transacoes> transacoes;
}
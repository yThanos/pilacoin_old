package br.ufsm.csi.tapw.pilacoin.model.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

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
public class BlocoJson {
    private int numeroBloco;
    private boolean minerado;
    private String nonceBlocoAnterior;
    private String nonce;
    private String nomeUsuarioMinerador;
    private byte[] chaveUsuarioMinerador;
    private List<Transacoes> transacoes;
}
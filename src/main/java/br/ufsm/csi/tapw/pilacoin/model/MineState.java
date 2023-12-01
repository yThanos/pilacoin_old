package br.ufsm.csi.tapw.pilacoin.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MineState {
    private boolean minerandoPila;
    private boolean minerandoBloco;
    private boolean validandoPila;
    private boolean validandoBloco;
}

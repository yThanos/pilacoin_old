package br.ufsm.csi.tapw.pilacoin.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LogsDTO {
    private List<Pilacoin> pilacoins;
    private List<Logs> logs;
}

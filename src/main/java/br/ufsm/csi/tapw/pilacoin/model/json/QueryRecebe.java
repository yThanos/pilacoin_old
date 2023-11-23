package br.ufsm.csi.tapw.pilacoin.model.json;

import br.ufsm.csi.tapw.pilacoin.model.Usuario;
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
public class QueryRecebe {
    private int idQuery;
    private String usuario;
    private List<PilaCoinJson> pilasResult;
    private List<BlocoJson> blocosResult;
    private List<Usuario> usuariosResult;
}

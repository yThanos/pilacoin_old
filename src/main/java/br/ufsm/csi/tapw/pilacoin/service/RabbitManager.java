package br.ufsm.csi.tapw.pilacoin.service;

import br.ufsm.csi.tapw.pilacoin.model.json.BlocoJson;
import br.ufsm.csi.tapw.pilacoin.model.json.MsgsJson;
import br.ufsm.csi.tapw.pilacoin.model.json.PilaCoinJson;
import br.ufsm.csi.tapw.pilacoin.model.json.ValidacaoPilaJson;
import br.ufsm.csi.tapw.pilacoin.util.Constants;
import br.ufsm.csi.tapw.pilacoin.util.PilaUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

@Service
public class RabbitManager {

    private final RabbitTemplate rabbitTemplate;

    private static ArrayList<String> pilaIgnroe = new ArrayList<>();
    public static ArrayList<MsgsJson> mensagens = new ArrayList<>();

    @Autowired
    public RabbitManager(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void descobreBloco(@Payload String blocoJson) throws JsonProcessingException, NoSuchAlgorithmException {
        System.out.println("Descobriu um bloco!");
        ObjectMapper om = new ObjectMapper();
        BlocoJson bloco = om.readValue(blocoJson, BlocoJson.class);
        BigInteger hash;
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        bloco.setNomeUsuarioMinerador(Constants.USERNAME);
        bloco.setChaveUsuarioMinerador(Constants.PUBLIC_KEY.toString().getBytes());
        boolean loop = true;
        while(loop){
            bloco.setNonce(new PilaUtil().geraNonce());
            hash = new BigInteger(md.digest(om.writeValueAsString(bloco).getBytes(StandardCharsets.UTF_8))).abs();
            if (hash.compareTo(Constants.DIFFICULTY) < 0){
                rabbitTemplate.convertAndSend(om.writeValueAsString(bloco));
                loop = false;
            }
        }
        MsgsJson msg = MsgsJson.builder().msg("Bloco descoberto e minerado!").
                lida(false).nomeUsuario(Constants.USERNAME).queue("Decobre bloco").build();
        //RabbitManager.mensagens.add(msg);
    }

    public void pilaMinerado(@Payload String pilaStr) throws NoSuchAlgorithmException {
        System.out.println("-=+=-=+=-=+=".repeat(4));
        ObjectMapper ob = new ObjectMapper();
        PilaCoinJson pilaJson;
        try {
            pilaJson = ob.readValue(pilaStr, PilaCoinJson.class);
        } catch (JsonProcessingException e) {
            rabbitTemplate.convertAndSend("pila-minerado", pilaStr);
            return;
        }
        if(pilaJson.getNomeCriador().equals("Vitor Fraporti")){
            System.out.println("Ignora é meu!");
            rabbitTemplate.convertAndSend("pila-minerado", pilaStr);
            return;
        } else {
            System.out.println("Validando pila do(a): "+pilaJson.getNomeCriador());
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            BigInteger hash = new BigInteger(md.digest(pilaStr.getBytes(StandardCharsets.UTF_8))).abs();
            if(hash.compareTo(Constants.DIFFICULTY) < 0){
                ValidacaoPilaJson validacaoPilaJson = ValidacaoPilaJson.builder().
                        pilaCoinJson(pilaJson).
                        assinaturaPilaCoin(new PilaUtil().getAssinatura(pilaStr)).
                        nomeValidador("Vitor Fraporti").
                        chavePublicaValidador(Constants.PUBLIC_KEY.toString().getBytes()).build();
                try {
                    System.out.println("Valido! :)");
                    rabbitTemplate.convertAndSend("pila-validado", ob.writeValueAsString(validacaoPilaJson));
                } catch (JsonProcessingException e) {
                    rabbitTemplate.convertAndSend("pila-minerado", pilaStr);
                    return;
                }
            } else {
                System.out.println("Não Validou! :(");
                rabbitTemplate.convertAndSend("pila-minerado", pilaStr);
            }
        }
        System.out.println("-=+=-=+=-=+=".repeat(4));
    }

    public void blocoMinerado(@Payload Service blocoJson){

    }

    public void mensagens(@Payload String msg){

    }
}

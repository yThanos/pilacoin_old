package br.ufsm.csi.tapw.pilacoin.service;

import br.ufsm.csi.tapw.pilacoin.model.Pilacoin;
import br.ufsm.csi.tapw.pilacoin.model.Usuario;
import br.ufsm.csi.tapw.pilacoin.model.json.*;
import br.ufsm.csi.tapw.pilacoin.repository.PilacoinRepository;
import br.ufsm.csi.tapw.pilacoin.repository.UsuarioRepository;
import br.ufsm.csi.tapw.pilacoin.util.Constants;
import br.ufsm.csi.tapw.pilacoin.util.PilaUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Optional;

@Service
public class RabbitManager {

    public static boolean minerandoBloco = true;
    public static boolean validandoPila = true;
    public static boolean validandoBloco = true;

    private final RabbitTemplate rabbitTemplate;
    public final UsuarioRepository usuarioRepository;
    public final PilacoinRepository pilacoinRepository;

    public RabbitManager(RabbitTemplate rabbitTemplate, UsuarioRepository usuarioRepository, PilacoinRepository pilacoinRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.usuarioRepository = usuarioRepository;
        this.pilacoinRepository = pilacoinRepository;
    }

    @RabbitListener(queues = "fraporti-query")
    public void algo(@Payload String msg) throws JsonProcessingException {
        //System.out.println("Resposta da query: "+msg);
        ObjectMapper objectMapper = new ObjectMapper();
        QueryRecebe qry = objectMapper.readValue(msg, QueryRecebe.class);
        if(qry.getPilasResult() != null){
            for (PilaCoinJson pila : qry.getPilasResult()){
                if(Arrays.equals(pila.getChaveCriador(), Constants.PUBLIC_KEY.getEncoded()) && (pila.getTransacoes() == null || pila.getTransacoes().size() <= 1)){
                    pilacoinRepository.save(Pilacoin.builder().status(pila.getStatus()).nonce(pila.getNonce()).build());
                } else if(pila.getTransacoes() != null){
                    if (Arrays.equals(pila.getTransacoes().get(pila.getTransacoes().size() -1).getChaveUsuarioDestino(), Constants.PUBLIC_KEY.getEncoded())){
                        pilacoinRepository.save(Pilacoin.builder().status(pila.getStatus()).nonce(pila.getNonce()).build());
                    } else if (Arrays.equals(pila.getTransacoes().get(pila.getTransacoes().size() -1).getChaveUsuarioOrigem(), Constants.PUBLIC_KEY.getEncoded())){
                        pilacoinRepository.delete(Pilacoin.builder().nonce(pila.getNonce()).status(pila.getStatus()).build());
                    }
                }
            }
        } else if (qry.getUsuariosResult() != null){
            usuarioRepository.saveAll(qry.getUsuariosResult());
        } else if (qry.getBlocosResult() != null){
            for (BlocoJson bloco : qry.getBlocosResult()){
                for (Transacoes transaccao: bloco.getTransacoes()){
                    if (transaccao.getChaveUsuarioDestino() != null && Arrays.equals(transaccao.getChaveUsuarioDestino(), Constants.PUBLIC_KEY.getEncoded())){
                        pilacoinRepository.save(Pilacoin.builder().nonce(transaccao.getNoncePila()).status("VALIDO").build());
                    } else if (transaccao.getChaveUsuarioOrigem() != null && Arrays.equals(transaccao.getChaveUsuarioOrigem(), Constants.PUBLIC_KEY.getEncoded())){
                        pilacoinRepository.delete(Pilacoin.builder().nonce(transaccao.getNoncePila()).status("VALIDO").build());
                    }
                }
            }
        }
    }

    @RabbitListener(queues = "descobre-bloco")
    public void descobreBloco(@Payload String blocoJson) throws JsonProcessingException, NoSuchAlgorithmException {
        if(!minerandoBloco){
            System.out.println("Ignorando bloco, minerando desativado");
            rabbitTemplate.convertAndSend("descobre-bloco", blocoJson);
            return;
        }
        System.out.println("=========".repeat(6));
        System.out.println("Descobriu um bloco!");
        System.out.println(blocoJson);
        ObjectMapper om = new ObjectMapper();
        BlocoJson bloco = om.readValue(blocoJson, BlocoJson.class);
        bloco.setNomeUsuarioMinerador(Constants.USERNAME);
        bloco.setChaveUsuarioMinerador(Constants.PUBLIC_KEY.getEncoded());
        BigInteger hash;
        while(true){
            bloco.setNonce(PilaUtil.geraNonce());
            hash = PilaUtil.geraHash(bloco);
            if (hash.compareTo(Constants.DIFFICULTY) < 0){
                System.out.println(hash);
                System.out.println(Constants.DIFFICULTY);
                System.out.println(blocoJson);
                System.out.println("Numero do bloco: "+bloco.getNumeroBloco());
                rabbitTemplate.convertAndSend("bloco-minerado",om.writeValueAsString(bloco));
                return;
            }
        }
    }

    @RabbitListener(queues = "pila-minerado")
    public void pilaMinerado(@Payload String pilaStr) throws NoSuchAlgorithmException, JsonProcessingException {
        if(!validandoPila){
            System.out.println("Ignorando pila, validação desativada");
            rabbitTemplate.convertAndSend("pila-minerado", pilaStr);
            return;
        }
        System.out.println("-=+=-=+=-=+=".repeat(4));
        ObjectMapper ob = new ObjectMapper();
        PilaCoinJson pilaJson = ob.readValue(pilaStr, PilaCoinJson.class);

        if(pilaJson.getNomeCriador().equals(Constants.USERNAME)){
            System.out.println("Ignora é meu!");
            rabbitTemplate.convertAndSend("pila-minerado", pilaStr);
        } else {
            System.out.println("Validando pila do(a): "+pilaJson.getNomeCriador());
            BigInteger hash = PilaUtil.geraHash(pilaStr);
            if(hash.compareTo(Constants.DIFFICULTY) < 0){
                ValidacaoPilaJson validacaoPilaJson = ValidacaoPilaJson.builder().
                        pilaCoinJson(pilaJson).
                        assinaturaPilaCoin(PilaUtil.geraAssinatura(pilaStr)).
                        nomeValidador(Constants.USERNAME).
                        chavePublicaValidador(Constants.PUBLIC_KEY.getEncoded()).build();
                rabbitTemplate.convertAndSend("pila-validado", ob.writeValueAsString(validacaoPilaJson));
                System.out.println("Valido!");
            } else {
                System.out.println("Não Validou! :(");
                rabbitTemplate.convertAndSend("pila-minerado", pilaStr);
            }
        }
        System.out.println("-=+=-=+=-=+=".repeat(4));
    }


    @RabbitListener(queues = "report")
    public void report(@Payload String report){
        System.out.println(report);
    }

    @RabbitListener(queues = "bloco-minerado")
    public void blocoMinerado(@Payload String blocoJson) throws NoSuchAlgorithmException, JsonProcessingException {
        if(!validandoBloco){
            System.out.println("Ignorando bloco, validação desativada");
            rabbitTemplate.convertAndSend("bloco-minerado", blocoJson);
            return;
        }
        System.out.println("XXXXXXXXXX".repeat(4));
        ObjectMapper om = new ObjectMapper();
        BlocoJson bloco;
        try {
            bloco = om.readValue(blocoJson, BlocoJson.class);
        } catch (JsonProcessingException e) {
            System.out.println("Bloco formato invalido!");
            return;
        }
        if (bloco.getNomeUsuarioMinerador() == null || bloco.getNomeUsuarioMinerador().equals(Constants.USERNAME)){
            System.out.println("Ignora meu bloco\n"+"XXXXXXXXXX".repeat(4));
            rabbitTemplate.convertAndSend("bloco-minerado",blocoJson);
            return;
        }
        System.out.println("Validando bloco do(a): "+bloco.getNomeUsuarioMinerador());
        BigInteger hash;
        try {
            hash = PilaUtil.geraHash(blocoJson);
        } catch (JsonProcessingException e) {
            return;
        }
        System.out.println(hash);
        System.out.println(Constants.DIFFICULTY);
        System.out.println("Numero do bloco: "+bloco.getNumeroBloco());
        if(hash.compareTo(Constants.DIFFICULTY) < 0){
            ValidacaoBlocoJson vbj = ValidacaoBlocoJson.builder().
                    assinaturaBloco(PilaUtil.geraAssinatura(blocoJson)).bloco(bloco).
                    chavePublicaValidador(Constants.PUBLIC_KEY.getEncoded()).
                    nomeValidador(Constants.USERNAME).build();
            System.out.println("Valido! :)");
            System.out.println("XXXXXXXXXX".repeat(4));
            try {
                rabbitTemplate.convertAndSend("bloco-validado", om.writeValueAsString(vbj));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Não validou :(");
        }
        System.out.println("XXXXXXXXXX".repeat(4));
    }

    @RabbitListener(queues = "fraporti")
    public void mensagens(@Payload String msg){
        System.out.println("-=+=".repeat(10)+"\n"+msg+"\n"+"-=+=".repeat(10));
    }
}

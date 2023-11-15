package br.ufsm.csi.tapw.pilacoin.service;

import br.ufsm.csi.tapw.pilacoin.model.Msgs;
import br.ufsm.csi.tapw.pilacoin.model.Pilacoin;
import br.ufsm.csi.tapw.pilacoin.model.Usuario;
import br.ufsm.csi.tapw.pilacoin.model.json.*;
import br.ufsm.csi.tapw.pilacoin.repository.MsgsRepository;
import br.ufsm.csi.tapw.pilacoin.repository.PilacoinRepository;
import br.ufsm.csi.tapw.pilacoin.repository.UsuarioRepository;
import br.ufsm.csi.tapw.pilacoin.util.Constants;
import br.ufsm.csi.tapw.pilacoin.util.PilaUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class RabbitManager {

    private final RabbitTemplate rabbitTemplate;
    private final PilacoinRepository pilacoinRepository;
    public final UsuarioRepository usuarioRepository;
    private static final ArrayList<String> listIgnroe = new ArrayList<>();
    private final MsgsRepository msgsRepository;

    @Autowired
    public RabbitManager(RabbitTemplate rabbitTemplate, PilacoinRepository pilacoinRepository, MsgsRepository msgsRepository, UsuarioRepository usuarioRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.pilacoinRepository = pilacoinRepository;
        this.msgsRepository = msgsRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @RabbitListener(queues = "descobre-bloco")
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
                System.out.println(hash);
                System.out.println(Constants.DIFFICULTY);
                rabbitTemplate.convertAndSend("bloco-minerado",om.writeValueAsString(bloco));
                loop = false;
            }
        }
        System.out.println("Bloco minerado");
        Msgs msg = Msgs.builder().msg("Bloco descoberto e minerado!").
                lida(false).nomeUsuario(Constants.USERNAME).queue("Decobre bloco").build();
        msgsRepository.save(msg);
    }

    @RabbitListener(queues = "pila-minerado")
    public void pilaMinerado(@Payload String pilaStr) throws NoSuchAlgorithmException {
        for(String pila: listIgnroe){
            if (pila.equals(pilaStr)){
                return;
            }
        }
        listIgnroe.add(pilaStr);
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

    @RabbitListener(queues = "bloco-minerado")
    public void blocoMinerado(@Payload String blocoJson) throws NoSuchAlgorithmException {
        for(String bloco: listIgnroe){
            if (bloco.equals(blocoJson)){
                return;
            }
        }
        listIgnroe.add(blocoJson);
        System.out.println("XXXXXXXXXX".repeat(4));
        System.out.println("Validando bloco!");
        ObjectMapper om = new ObjectMapper();
        BlocoJson bloco;
        try {
            bloco = om.readValue(blocoJson, BlocoJson.class);
        } catch (JsonProcessingException e) {
            System.out.println("Erro conversão");
            return;
        }
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        BigInteger hash = new BigInteger(md.digest(blocoJson.getBytes(StandardCharsets.UTF_8))).abs();
        System.out.println(hash);
        System.out.println(Constants.DIFFICULTY);
        if(hash.compareTo(Constants.DIFFICULTY) < 0){
            ValidacaoBlocoJson vbj = ValidacaoBlocoJson.builder().
                    assinaturaBloco(new PilaUtil().getAssinatura(blocoJson)).bloco(bloco).
                    chavePublicaValidador(Constants.PUBLIC_KEY.toString().getBytes()).
                    nomeValidador(Constants.USERNAME).build();
            try {
                System.out.println("Valido! :)");
                System.out.println("XXXXXXXXXX".repeat(4));
                rabbitTemplate.convertAndSend("bloco-validado", om.writeValueAsString(vbj));
            } catch (JsonProcessingException e) {
                System.out.println("Erro conversão");
                return;
            }
        } else {
            System.out.println("Não validou :(");
        }
        System.out.println("XXXXXXXXXX".repeat(4));
    }

    @RabbitListener(queues = "vitor_fraporti")
    public void mensagens(@Payload String msg) throws JsonProcessingException {
        Msgs message = new ObjectMapper().readValue(msg, Msgs.class);
        msgsRepository.save(message);
        System.out.println("-=+=".repeat(10));
        System.out.println(msg);
        System.out.println("-=+=".repeat(10));
    }

    @RabbitListener(queues = "Vitor Fraporti-bloco-validado")
    public void blocoMsg(@Payload String msg) throws JsonProcessingException {
        System.out.println("Msg bloco validado: "+msg);
        ObjectMapper om = new ObjectMapper();
        ValidacaoBlocoJson validacaoBlocoJson = om.readValue(msg, ValidacaoBlocoJson.class);
        for(Transacoes transacao : validacaoBlocoJson.getBloco().getTransacoes()){
            if(transacao.getNomeUsuarioOrigem().equals(Constants.USERNAME)){
                //ToDo: remove do banco o meu pilacoin
            } else if (transacao.getNomeUsuarioDestino().equals(Constants.USERNAME)){
                //ToDo: insere no banco o meu pilaocin
            }
        }
    }

    @RabbitListener(queues = "Vitor Fraporti-pila-validado")
    public void pilaMsg(@Payload String msg) throws JsonProcessingException {
        System.out.println("Msg pila validado: "+msg);
        ObjectMapper om = new ObjectMapper();
        ValidacaoPilaJson vpj = om.readValue(msg,ValidacaoPilaJson.class);
        Optional<Usuario> user = usuarioRepository.findByNome(vpj.getPilaCoinJson().getNomeCriador());
        if (user.isEmpty()){
            usuarioRepository.save(Usuario.builder().nome(vpj.getPilaCoinJson().getNomeCriador()).
                    chavePublciaUsuario(vpj.getPilaCoinJson().getChaveCriador()).build());
            user = usuarioRepository.findByNome(vpj.getPilaCoinJson().getNomeCriador());
        }
        user.ifPresent(usuario -> pilacoinRepository.save(Pilacoin.builder().nonce(vpj.getPilaCoinJson().getNonce()).
                idDono(usuario).build()));
    }
}

package br.ufsm.csi.tapw.pilacoin.service;

import br.ufsm.csi.tapw.pilacoin.model.Pilacoin;
import br.ufsm.csi.tapw.pilacoin.model.json.PilaCoinJson;
import br.ufsm.csi.tapw.pilacoin.repository.PilacoinRepository;
import br.ufsm.csi.tapw.pilacoin.util.Constants;
import br.ufsm.csi.tapw.pilacoin.util.PilaUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;

public class Mineradora extends Thread {
    private final RabbitTemplate rabbitTemplate;
    private final PilacoinRepository pilacoinRepository;
    private volatile boolean minerando = false;

    public Mineradora(RabbitTemplate rabbitTemplate, PilacoinRepository pilacoinRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.pilacoinRepository = pilacoinRepository;
    }

    @SneakyThrows
    public void run() {
        PilaCoinJson pj = PilaCoinJson.builder().dataCriacao(new Date()).
                chaveCriador(Constants.PUBLIC_KEY.getEncoded()).
                nomeCriador(Constants.USERNAME).build();
        ObjectMapper om = new ObjectMapper();
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        BigInteger hash;
        int tentativa = 0;
        while (true){
            tentativa++;
            if(!minerando){
                while (!minerando){}
            }
            pj.setNonce(PilaUtil.geraNonce());
            hash = new BigInteger(md.digest(om.writeValueAsString(pj).getBytes(StandardCharsets.UTF_8))).abs();
            if (hash.compareTo(Constants.DIFFICULTY) < 0){
                System.out.println("\n\nMINERADO em "+tentativa+" tentativas\n\n");
                tentativa = 0;
                rabbitTemplate.convertAndSend("pila-minerado", om.writeValueAsString(pj));
                pilacoinRepository.save(Pilacoin.builder().nonce(pj.getNonce()).status("AG_VALIDACAO").build());
            }
        }
    }

    public void parar(){
        minerando = false;
    }

    public void continuar(){
        minerando = true;
    }
}

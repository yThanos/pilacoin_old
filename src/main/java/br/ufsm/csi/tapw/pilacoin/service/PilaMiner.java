package br.ufsm.csi.tapw.pilacoin.service;

import br.ufsm.csi.tapw.pilacoin.model.json.MsgsJson;
import br.ufsm.csi.tapw.pilacoin.model.json.PilaCoinJson;
import br.ufsm.csi.tapw.pilacoin.util.Constants;
import br.ufsm.csi.tapw.pilacoin.util.PilaUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;

@Service
public class PilaMiner {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void mina(){
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                PilaCoinJson pj = PilaCoinJson.builder().dataCriacao(new Date()).
                        chaveCriador(Constants.PUBLIC_KEY.toString().getBytes()).
                        nomeCriador(Constants.USERNAME).build();
                ObjectMapper om = new ObjectMapper();
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                BigInteger hash;
                while (true){
                    pj.setNonce(new PilaUtil().geraNonce());
                    hash = new BigInteger(md.digest(om.writeValueAsString(pj).getBytes(StandardCharsets.UTF_8))).abs();
                    if (hash.compareTo(Constants.DIFFICULTY) < 0){
                        new Thread(new SalvaPila(rabbitTemplate, om.writeValueAsString(pj))).start();
                    }
                }
            }
        }).start();
    }

    private record SalvaPila(RabbitTemplate rabbitTemplate, String pila) implements Runnable {
        @Override
            public void run() {
                rabbitTemplate.convertAndSend("pila-minerado", pila);
                RabbitManager.mensagens.add(MsgsJson.builder().msg("Pila minerado!").nomeUsuario(Constants.USERNAME).lida(false).build());
                //ToDo: salvar meu pila no banco
            }
        }
}

package br.ufsm.csi.tapw.pilacoin.service;

import br.ufsm.csi.tapw.pilacoin.model.Msgs;
import br.ufsm.csi.tapw.pilacoin.model.Pilacoin;
import br.ufsm.csi.tapw.pilacoin.model.json.PilaCoinJson;
import br.ufsm.csi.tapw.pilacoin.repository.MsgsRepository;
import br.ufsm.csi.tapw.pilacoin.repository.PilacoinRepository;
import br.ufsm.csi.tapw.pilacoin.util.Constants;
import br.ufsm.csi.tapw.pilacoin.util.PilaUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.util.Date;

import static br.ufsm.csi.tapw.pilacoin.controller.TesteController.threadMineradora;

@Service
public class PilaMiner {
    private final RabbitTemplate rabbitTemplate;
    private final PilacoinRepository pilacoinRepository;

    public PilaMiner(RabbitTemplate rabbitTemplate, PilacoinRepository pilacoinRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.pilacoinRepository = pilacoinRepository;
    }

    public static Thread threadMiner;

    @PostConstruct
    public void mina(){
        KeyPair kp;
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("keypair.ser"))) {
            kp = (KeyPair) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Erro carregando as chaves");
            return;
        }
        Constants.PUBLIC_KEY = kp.getPublic();
        Constants.PRIVATE_KEY = kp.getPrivate();
        threadMineradora = new Mineradora(rabbitTemplate, pilacoinRepository);
        threadMineradora.setName("Mineradora");
        threadMineradora.start();
        threadMiner = new Thread(new Runnable() {
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
                        System.out.println("\n\nMINERADO\n\n");
                        rabbitTemplate.convertAndSend("pila-minerado", om.writeValueAsString(pj));
                        pilacoinRepository.save(Pilacoin.builder().nonce(pj.getNonce()).status("MINERADO").build());
                    }
                }
            }
        });
        //threadMiner.start();
    }
}


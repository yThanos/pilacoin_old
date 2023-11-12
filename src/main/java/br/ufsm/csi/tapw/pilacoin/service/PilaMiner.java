package br.ufsm.csi.tapw.pilacoin.service;

import br.ufsm.csi.tapw.pilacoin.model.Pilacoin;
import br.ufsm.csi.tapw.pilacoin.model.Usuario;
import br.ufsm.csi.tapw.pilacoin.model.json.MsgsJson;
import br.ufsm.csi.tapw.pilacoin.model.json.PilaCoinJson;
import br.ufsm.csi.tapw.pilacoin.repository.PilacoinRepository;
import br.ufsm.csi.tapw.pilacoin.util.Constants;
import br.ufsm.csi.tapw.pilacoin.util.PilaUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.util.Date;

@Service
public class PilaMiner {
    @Autowired
    private RabbitTemplate rabbitTemplate;

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
                int tentativas = 0;
                while (true){
                    tentativas++;
                    pj.setNonce(new PilaUtil().geraNonce());
                    hash = new BigInteger(md.digest(om.writeValueAsString(pj).getBytes(StandardCharsets.UTF_8))).abs();
                    if (hash.compareTo(Constants.DIFFICULTY) < 0){
                        System.out.println("Minerado em: "+tentativas);
                        System.out.println("HASH: "+hash);
                        System.out.println("DIFF: "+Constants.DIFFICULTY);
                        System.out.println("NONCE: "+pj.getNonce());
                        rabbitTemplate.convertAndSend("pila-minerado", om.writeValueAsString(pj));
                        RabbitManager.mensagens.add(MsgsJson.builder().msg("Pila minerado!").nomeUsuario(Constants.USERNAME).lida(false).build());
                        tentativas = 0;
                    }
                }
            }
        }).start();
    }

    private record SalvaPila(RabbitTemplate rabbitTemplate, PilaCoinJson pila, PilacoinRepository pilacoinRepository) implements Runnable {
        @SneakyThrows
        @Override
        public void run() {
            ObjectMapper ob = new ObjectMapper();
            rabbitTemplate.convertAndSend("pila-minerado", ob.writeValueAsString(pila));
            RabbitManager.mensagens.add(MsgsJson.builder().msg("Pila minerado!").nomeUsuario(Constants.USERNAME).lida(false).build());
            //ToDo: salvar meu pila no banco
            pilacoinRepository.save(Pilacoin.builder().nonce(pila.getNonce()).idDono(Usuario.builder().id(1L).build()).build());
            //System.out.println("Pila minerado\n");
        }
    }
}

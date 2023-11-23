package br.ufsm.csi.tapw.pilacoin.service;

import br.ufsm.csi.tapw.pilacoin.repository.PilacoinRepository;
import br.ufsm.csi.tapw.pilacoin.util.Constants;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.KeyPair;

import static br.ufsm.csi.tapw.pilacoin.controller.TesteController.threadMineradora;

@Service
public class Initializer {

    public Initializer(RabbitTemplate rabbitTemplate, PilacoinRepository pilacoinRepository) {
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
    }

}


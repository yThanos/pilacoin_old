package br.ufsm.csi.tapw.pilacoin.controller;

import br.ufsm.csi.tapw.pilacoin.model.Pilacoin;
import br.ufsm.csi.tapw.pilacoin.model.Usuario;
import br.ufsm.csi.tapw.pilacoin.model.Msgs;
import br.ufsm.csi.tapw.pilacoin.model.json.TransferirPilaJson;
import br.ufsm.csi.tapw.pilacoin.repository.MsgsRepository;
import br.ufsm.csi.tapw.pilacoin.repository.PilacoinRepository;
import br.ufsm.csi.tapw.pilacoin.repository.UsuarioRepository;
import br.ufsm.csi.tapw.pilacoin.service.Mineradora;
import br.ufsm.csi.tapw.pilacoin.service.PilaMiner;
import br.ufsm.csi.tapw.pilacoin.util.Constants;
import br.ufsm.csi.tapw.pilacoin.util.PilaUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/teste")
@CrossOrigin
public class TesteController {
    private final PilacoinRepository pilacoinRepository;
    private final UsuarioRepository usuarioRepository;
    private final MsgsRepository msgsRepository;
    private final RabbitTemplate rabbitTemplate;
    public static Mineradora threadMineradora;
    public boolean minernado = true;

    public TesteController(PilacoinRepository pilacoinRepository, UsuarioRepository usuarioRepository, MsgsRepository msgsRepository, RabbitTemplate rabbitTemplate) {
        this.pilacoinRepository = pilacoinRepository;
        this.usuarioRepository = usuarioRepository;
        this.msgsRepository = msgsRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @GetMapping("/minerar")
    public boolean minerar(){
        if(minernado){
            threadMineradora.parar();
        } else {
            threadMineradora.continuar();
        }
        System.out.println(threadMineradora.getState());
        minernado = !minernado;
        return minernado;
    }

    @GetMapping("/users")
    public List<Usuario> getUser(){
        System.out.println(usuarioRepository.findAll());
        return usuarioRepository.findAll();
    }

    @GetMapping("/mineState")
    public boolean isMinernado(){
        return minernado;
    }

    @GetMapping("/pilas")
    public List<Pilacoin> getPilas(){
        return pilacoinRepository.findAll();
    }

    @GetMapping("/addUser")
    public void addUser(){
        usuarioRepository.save(Usuario.builder().nome(Constants.USERNAME).
                chavePublciaUsuario(Constants.PUBLIC_KEY.toString().getBytes()).build());
    }

    @GetMapping("/msgs")
    public List<Msgs> getMsgs(){
        return msgsRepository.findAll();
    }

    @PostMapping("/tranferir/{qtd}")
    public void transferirPila(@RequestBody Usuario user, @PathVariable int qtd) throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        Optional<List<Pilacoin>> pilas = pilacoinRepository.findAllByStatusEquals("VALIDO");
        if(pilas.isPresent()){
            for (int i = 0;i<qtd;i++){
                Pilacoin pila = pilas.get().get(i);
                TransferirPilaJson tpj = TransferirPilaJson.builder().chaveUsuarioDestino(user.getChavePublciaUsuario()).
                        chaveUsuarioOrigem(Constants.PUBLIC_KEY.getEncoded()).noncePila(pila.getNonce()).
                        nomeUsuarioDestino(user.getNome()).nomeUsuarioOrigem(Constants.USERNAME).
                        dataTransacao(new Date()).build();
                tpj.setAssinatura(new PilaUtil().getAssinatura(tpj));
                rabbitTemplate.convertAndSend("transferir-pila", om.writeValueAsString(tpj));
                pilacoinRepository.save(Pilacoin.builder().nonce(pila.getNonce()).status("TRANSFERIDO").build());
            }
        }
    }

    @GetMapping("/query/{query}")
    public void query(@PathVariable String query){
        rabbitTemplate.convertAndSend("Vitor Fraporti-query", query);
    }
}

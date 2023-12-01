package br.ufsm.csi.tapw.pilacoin.controller;

import br.ufsm.csi.tapw.pilacoin.model.Pilacoin;
import br.ufsm.csi.tapw.pilacoin.model.Usuario;
import br.ufsm.csi.tapw.pilacoin.model.MineState;
import br.ufsm.csi.tapw.pilacoin.model.json.QueryEnvia;
import br.ufsm.csi.tapw.pilacoin.model.json.TransferirPilaJson;
import br.ufsm.csi.tapw.pilacoin.repository.PilacoinRepository;
import br.ufsm.csi.tapw.pilacoin.repository.UsuarioRepository;
import br.ufsm.csi.tapw.pilacoin.service.Mineradora;
import br.ufsm.csi.tapw.pilacoin.service.RabbitManager;
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
    private final RabbitTemplate rabbitTemplate;
    public static Mineradora threadMineradora;
    public boolean minernado = true;

    public TesteController(PilacoinRepository pilacoinRepository, UsuarioRepository usuarioRepository, RabbitTemplate rabbitTemplate) {
        this.pilacoinRepository = pilacoinRepository;
        this.usuarioRepository = usuarioRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @GetMapping("/minerar")
    public boolean minerar(){
        System.out.println("Alterando mineração");
        if(minernado){
            threadMineradora.parar();
        } else {
            threadMineradora.continuar();
        }
        minernado = !minernado;
        return minernado;
    }

    @GetMapping("/users")
    public List<Usuario> getUser(){
        System.out.println(usuarioRepository.findAll());
        return usuarioRepository.findAll();
    }

    @GetMapping("/mineState")
    public MineState isMinernado(){
        return MineState.builder().minerandoPila(minernado).minerandoBloco(RabbitManager.minerandoBloco)
            .validandoBloco(RabbitManager.validandoBloco).validandoPila(RabbitManager.validandoPila).build();
    }

    @GetMapping("/pilas")
    public List<Pilacoin> getPilas(){
        return pilacoinRepository.findAll();
    }

    @PostMapping("/tranferir/{qtd}")
    public void transferirPila(@RequestBody Usuario user, @PathVariable int qtd) throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        Optional<List<Pilacoin>> pilas = pilacoinRepository.findAllByStatusEquals("VALIDO");
        if(pilas.isPresent() && pilas.get().size() >= qtd){
            for (int i = 0;i<qtd;i++){
                Pilacoin pila = pilas.get().get(i);
                TransferirPilaJson tpj = TransferirPilaJson.builder().chaveUsuarioDestino(user.getChavePublica()).
                        chaveUsuarioOrigem(Constants.PUBLIC_KEY.getEncoded()).noncePila(pila.getNonce()).
                        nomeUsuarioDestino(user.getNome()).nomeUsuarioOrigem(Constants.USERNAME).
                        dataTransacao(new Date()).build();
                tpj.setAssinatura(PilaUtil.geraAssinatura(tpj));
                System.out.println(om.writeValueAsString(tpj));
                rabbitTemplate.convertAndSend("transferir-pila", om.writeValueAsString(tpj));
                pilacoinRepository.delete(pila);
            }
        }
    }

    @GetMapping("/query/{tipo}")
    public void query(@PathVariable String tipo) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        rabbitTemplate.convertAndSend("query", objectMapper.writeValueAsString(QueryEnvia.builder().
                tipoQuery(tipo).idQuery(1).nomeUsuario(Constants.USERNAME).build()));
    }

    @GetMapping("/minerarBloco")
    public boolean minerarBloco(){
        System.out.println("Alterando mineração de bloco");
        RabbitManager.minerandoBloco = !RabbitManager.minerandoBloco;
        return RabbitManager.minerandoBloco;
    }

    @GetMapping("/validarBloco")
    public boolean validarBloco(){
        System.out.println("Alterando validação de bloco");
        RabbitManager.validandoBloco = !RabbitManager.validandoBloco;
        return RabbitManager.validandoBloco;
    }

    @GetMapping("/validarPila")
    public boolean validarPila(){
        System.out.println("Alterando validação de pila");
        RabbitManager.validandoPila = !RabbitManager.validandoPila;
        return RabbitManager.validandoPila;
    }
}

package br.ufsm.csi.tapw.pilacoin.controller;

import br.ufsm.csi.tapw.pilacoin.model.Pilacoin;
import br.ufsm.csi.tapw.pilacoin.model.Usuario;
import br.ufsm.csi.tapw.pilacoin.model.Msgs;
import br.ufsm.csi.tapw.pilacoin.model.json.TransferirPilaJson;
import br.ufsm.csi.tapw.pilacoin.repository.MsgsRepository;
import br.ufsm.csi.tapw.pilacoin.repository.PilacoinRepository;
import br.ufsm.csi.tapw.pilacoin.repository.UsuarioRepository;
import br.ufsm.csi.tapw.pilacoin.util.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teste")
@CrossOrigin
public class TesteController {
    private final PilacoinRepository pilacoinRepository;
    private final UsuarioRepository usuarioRepository;
    private final MsgsRepository msgsRepository;
    private final RabbitTemplate rabbitTemplate;

    public TesteController(PilacoinRepository pilacoinRepository, UsuarioRepository usuarioRepository, MsgsRepository msgsRepository, RabbitTemplate rabbitTemplate) {
        this.pilacoinRepository = pilacoinRepository;
        this.usuarioRepository = usuarioRepository;
        this.msgsRepository = msgsRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @GetMapping("/pilas")
    public List<Pilacoin> getPilas(){
        return pilacoinRepository.findAll();
    }

    @GetMapping("/addUser")
    public void addUser(){
        usuarioRepository.save(Usuario.builder().nome(Constants.USERNAME).chavePublciaUsuario(Constants.PUBLIC_KEY.toString().getBytes()).build());
    }

    @GetMapping("/msgs")
    public List<Msgs> getMsgs(){
        return msgsRepository.findAll();
    }

    @PostMapping("/tranferir")
    public void transferirPila(TransferirPilaJson tp) throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        rabbitTemplate.convertAndSend("transferir-pila", om.writeValueAsString(tp));
    }
}

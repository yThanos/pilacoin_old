package br.ufsm.csi.tapw.pilacoin.controller;

import br.ufsm.csi.tapw.pilacoin.model.Pilacoin;
import br.ufsm.csi.tapw.pilacoin.model.Usuario;
import br.ufsm.csi.tapw.pilacoin.repository.PilacoinRepository;
import br.ufsm.csi.tapw.pilacoin.repository.UsuarioRepository;
import br.ufsm.csi.tapw.pilacoin.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/teste")
@CrossOrigin
public class TesteController {
    @Autowired
    private PilacoinRepository pilacoinRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;
    @GetMapping("/pilas")
    public List<Pilacoin> getPilas(){
        return pilacoinRepository.findAll();
    }

    @GetMapping("addUser")
    public void addUser(){
        usuarioRepository.save(Usuario.builder().nomeUsuario(Constants.USERNAME).chavePublciaUsuario(Constants.PUBLIC_KEY.toString().getBytes()).build());
    }
    @GetMapping("/hello")
    public String hello(){
        return "Hello world!";
    }
}

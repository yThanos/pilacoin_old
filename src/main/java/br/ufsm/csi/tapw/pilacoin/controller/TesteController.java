package br.ufsm.csi.tapw.pilacoin.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teste")
@CrossOrigin
public class TesteController {
    @GetMapping("/hello")
    public String hello(){
        return "Hello world!";
    }
}

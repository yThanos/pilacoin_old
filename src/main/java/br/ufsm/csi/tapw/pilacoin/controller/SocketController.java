package br.ufsm.csi.tapw.pilacoin.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class SocketController {

    @MessageMapping
    @SendTo("/teste/socket")
    public String teste(){
        return "";
    }
}

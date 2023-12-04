package br.ufsm.csi.tapw.pilacoin.controller;

import br.ufsm.csi.tapw.pilacoin.model.LogsDTO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SocketController {
    @MessageMapping("/message")
    @SendTo("/topic/data")
    public LogsDTO transmiteMessage(@Payload LogsDTO logs){
        return  logs;
    }
}

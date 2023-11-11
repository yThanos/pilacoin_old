package br.ufsm.csi.tapw.pilacoin.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Random;

public class PilaUtil {
    @SneakyThrows
    public byte[] getAssinatura(Object object){
        ObjectMapper om = new ObjectMapper();
        String strObj = om.writeValueAsString(object);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, Constants.PRIVATE_KEY);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] assinatura = md.digest(strObj.getBytes(StandardCharsets.UTF_8));
        return cipher.doFinal(assinatura);
    }

    public String geraNonce(){
        Random rnd = new Random();
        byte[] bytes = new byte[256/8];
        rnd.nextBytes(bytes);
        return new BigInteger(bytes).abs().toString();
    }
}

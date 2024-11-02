package com.example.mdp.service;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class HashEncoder implements IEncoder {
    @Override
    public String getCode(String str) {
        return BCrypt.hashpw(str, BCrypt.gensalt());
    }

    @Override
    public boolean checkPassword(String passwordFromDB, String inputPassword) {
        return BCrypt.checkpw(inputPassword, passwordFromDB);
    }
}

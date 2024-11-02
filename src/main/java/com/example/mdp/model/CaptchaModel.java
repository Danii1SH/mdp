package com.example.mdp.model;

import lombok.Data;

@Data
public class CaptchaModel {
    private String captcha;
    private String hiddenCaptcha;
    private String realCaptcha;

}
package com.example.mdp.controller;

import cn.apiclub.captcha.Captcha;
import com.example.mdp.model.CaptchaModel;
import com.example.mdp.model.User;
import com.example.mdp.repository.UsersRepository;
import com.example.mdp.service.CaptchaGenerator;
import com.example.mdp.service.HashEncoder;
import com.example.mdp.service.IEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/registration")
public class RegistrationController {

    private final UsersRepository db;
    private final IEncoder encoder;

    public RegistrationController(UsersRepository db) {
        this.db = db;
        this.encoder = new HashEncoder();
    }

    private CaptchaModel generateCaptcha() {
        CaptchaModel captchaSettings = new CaptchaModel();
        Captcha captcha = CaptchaGenerator.generationCaptcha(260, 80);
        captchaSettings.setHiddenCaptcha(captcha.getAnswer());
        captchaSettings.setCaptcha("");
        captchaSettings.setRealCaptcha(CaptchaGenerator.encodeCaptchaBinary(captcha));
        return captchaSettings;
    }

    @GetMapping("/")
    public String registration(@ModelAttribute("error") String error, Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("captcha", generateCaptcha());
        return "registration";
    }

    @PostMapping("/addUser")
    public String addUser(@ModelAttribute("user") User newUser, @ModelAttribute("captcha") CaptchaModel captcha, Model model) {
        User userFromBd = db.getUserByLogin(newUser.getLogin());

        if(userFromBd != null) {
            String error = "Пользователь с таким логином уже есть";
            model.addAttribute("error", error);
            model.addAttribute("captcha",generateCaptcha());
            return "registration";
        }

        if(!newUser.getPassword().equals(newUser.getConfirmPassword())) {
            String error = "Пароли не совпадают";
            model.addAttribute("error", error);
            model.addAttribute("captcha",generateCaptcha());
            return "registration";
        }

        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(newUser.getEmail());

        if (!matcher.matches()) {
            String error = "Неправильный формат адреса электронной почты";
            model.addAttribute("error", error);
            model.addAttribute("captcha",generateCaptcha());
            return "registration";
        }

        if(!captcha.getCaptcha().equals(captcha.getHiddenCaptcha())){
            model.addAttribute("error","Текст капчи введен не верно");
            model.addAttribute("captcha",generateCaptcha());
            return "registration";
        }

        newUser.setPassword(encoder.getCode(newUser.getPassword()));
        newUser.setDarkTheme(false);
        db.save(newUser);
        return "redirect:/authorization/";
    }
}


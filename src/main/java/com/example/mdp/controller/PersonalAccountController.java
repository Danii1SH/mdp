package com.example.mdp.controller;


import com.example.mdp.model.User;
import com.example.mdp.repository.UsersRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/personalAccount")
@SessionAttributes("currentUser")
public class PersonalAccountController {

    private final UsersRepository db;

    public PersonalAccountController(UsersRepository db) {
        this.db = db;
    }

    @GetMapping("/")
    public String personalAccount(Model model) {
        User user = (User) model.getAttribute("currentUser");
        if (user != null) {
            return "personalAccount";
        }
        else return "redirect:/authorization/";
    }

    @PostMapping("/")
    public String setThemeOnUser(@ModelAttribute("currentUser") User user) {
        db.updateThemeOnUser(user);
        return "personalAccount";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        session.removeAttribute("currentUser");
        session.invalidate();

        Cookie cookie = new Cookie("loggedInUser", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/authorization/";
    }
}

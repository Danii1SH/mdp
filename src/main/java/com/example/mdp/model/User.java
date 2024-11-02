package com.example.mdp.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private int Id;
    private String firstName;
    private String secondName;
    private String email;
    private String login;
    private String password;
    private String confirmPassword;
    private String gender;
    private boolean darkTheme;
    private String creationDate;

    @Builder
    public User(int id, String firstName, String secondName, String email,
                String login, String password,String gender, String theme, String creationDate) {
        Id = id;
        this.firstName = firstName;
        this.secondName = secondName;
        this.email = email;
        this.login = login;
        this.password = password;
        this.gender = gender;
        this.darkTheme = theme.equals("Dark");
        this.creationDate = creationDate;
    }

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }
}

package com.example.client.entity;

import lombok.Data;

@Data
public abstract class User {
    protected String firstName;
    protected String lastName;
    protected String email;
    protected String password;
    protected String role;
}

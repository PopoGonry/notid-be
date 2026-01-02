package com.popogonry.notid.user.dto;

import com.popogonry.notid.user.Gender;
import com.popogonry.notid.user.User;
import lombok.Getter;

@Getter
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private Gender gender;
    private String phone;

    public UserResponse(Long id, String email, String name, Gender gender, String phone) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.gender = gender;
        this.phone = phone;
    }
}

package com.popogonry.notid.user.dto;

import com.popogonry.notid.user.Gender;
import com.popogonry.notid.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private Gender gender;
    private String phone;

}

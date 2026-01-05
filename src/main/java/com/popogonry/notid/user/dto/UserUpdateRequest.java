package com.popogonry.notid.user.dto;


import com.popogonry.notid.user.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @NotBlank
    private String name;

    @NotNull
    private Gender gender;

    @NotBlank
    private String phone;

    private List<Long> organizationIds = new ArrayList<>();
}

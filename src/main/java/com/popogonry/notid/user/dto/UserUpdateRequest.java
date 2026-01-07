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

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotNull(message = "성별은 필수입니다.")
    private Gender gender;

    @NotBlank(message = "연락처는 필수입니다.")
    private String phone;

    private List<Long> organizationIds = new ArrayList<>();
}

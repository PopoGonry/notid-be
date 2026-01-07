package com.popogonry.notid.user.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordUpdateRequest {

    @NotBlank(message = "기존 비밀번호는 필수입니다.")
    private String oldPassword;


    @NotBlank(message = "새로운 비밀번호는 필수입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
            message = "비밀번호는 8~20자 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String newPassword;

    @NotBlank(message = "새로운 비밀번호 확인은 필수입니다.")
    private String newPasswordConfirm;

    public boolean isNewPasswordSame() {
        return newPassword.equals(newPasswordConfirm);
    }

}

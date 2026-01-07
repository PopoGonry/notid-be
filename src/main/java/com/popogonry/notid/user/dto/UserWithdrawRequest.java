package com.popogonry.notid.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserWithdrawRequest {
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

    @NotBlank(message = "확인 비밀번호는 필수입니다.")
    private String confirmPassword;

    public boolean isPasswordSame() {
        return password.equals(confirmPassword);
    }
}

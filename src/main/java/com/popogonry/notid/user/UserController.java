package com.popogonry.notid.user;


import com.popogonry.notid.user.dto.UserSignUpRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<Long> signup(@RequestBody @Valid UserSignUpRequest request) {
        // @RequestBody: JSON 데이터를 DTO로 변환
        // @Valid: DTO 안에 있는 @NotBlank, @Email 같은 조건 검사

        Long userId = userService.signUp(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(userId);
    }

}

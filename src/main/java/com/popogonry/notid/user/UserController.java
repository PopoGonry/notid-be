package com.popogonry.notid.user;


import com.popogonry.notid.user.dto.UserSignInRequest;
import com.popogonry.notid.user.dto.UserSignInResponse;
import com.popogonry.notid.user.dto.UserSignUpRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/signin")
    public ResponseEntity<UserSignInResponse> signin(@RequestBody @Valid UserSignInRequest request) {
        String token = userService.signIn(request);
        return ResponseEntity.ok(new UserSignInResponse(token));
    }

    @GetMapping("/test")
    public String test() {
        return "로그인 성공! 이 글이 보이면 인증 완료된 것입니다. 🎉";
    }
}

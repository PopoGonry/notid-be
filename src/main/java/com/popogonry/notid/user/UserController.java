package com.popogonry.notid.user;


import com.popogonry.notid.user.dto.UserSignInRequest;
import com.popogonry.notid.user.dto.UserSignInResponse;
import com.popogonry.notid.user.dto.UserSignUpRequest;
import com.popogonry.notid.user.dto.UserUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

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

    @PatchMapping("/{userId}")
    public ResponseEntity<Long> updateInfo(
            @PathVariable Long userId,
            @RequestBody @Valid UserUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails// 현재 로그인한 사람
    ) throws AccessDeniedException {
        userService.updateInfo(userId, request, userDetails.getUsername());
        return ResponseEntity.ok(userId);
    }


    // JWT 토큰 정보에서 데이터를 뽑아와서, 자신의 정보만 수정 가능한 형태
//    @PatchMapping("/me")
//    public ResponseEntity<Long> updateInfo(
//            @RequestBody @Valid UserUpdateRequest request,
//            @AuthenticationPrincipal UserDetails userDetails
//    ) {
//        Long updatedId = userService.updateInfo(request, userDetails.getUsername());
//
//        return ResponseEntity.ok(updatedId);
//    }


    @GetMapping("/test")
    public String test() {
        return "로그인 성공! 이 글이 보이면 인증 완료된 것입니다. 🎉";
    }
}

package com.popogonry.notid.user;


import com.popogonry.notid.user.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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

        Long signedUpUserId = userService.signUp(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(signedUpUserId);
    }

    @PostMapping("/signin")
    public ResponseEntity<UserSignInResponse> signIn(@RequestBody @Valid UserSignInRequest request) {
        String token = userService.signIn(request);
        return ResponseEntity.ok(new UserSignInResponse(token));
    }

    @PostMapping("/signout")
    public ResponseEntity<Void> signOut() {
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable("userId") Long userId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userService.getUser(userId, userDetails.getUsername());
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Long> updateInfo(
            @PathVariable Long userId,
            @RequestBody @Valid UserUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails // 현재 로그인한 사람
    ) {
        Long updatedUserId = userService.updateInfo(userId, request, userDetails.getUsername());
        return ResponseEntity.ok(updatedUserId);
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

    @PatchMapping("/{userId}/password")
    public ResponseEntity<Long> updatePassword(
            @PathVariable Long userId,
            @RequestBody @Valid UserPasswordUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long updatedUserId = userService.updatePassword(userId, request, userDetails.getUsername());
        return ResponseEntity.ok(updatedUserId);

    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Long> deleteUser(
            @PathVariable Long userId,
            @RequestBody @Valid UserWithdrawRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long deletedUserId = userService.withdraw(userId, request, userDetails.getUsername());
        return ResponseEntity.ok(deletedUserId);
    }

    @GetMapping("/check-duplicate")
    public ResponseEntity<Boolean> checkDuplicate(
            @RequestParam String type,
            @RequestParam String value
    ) {
        boolean isDuplicate = userService.validateDuplicate(type, value);
        return ResponseEntity.ok(isDuplicate);
    }

    @GetMapping("/test")
    public String test() {
        return "로그인 성공! 이 글이 보이면 인증 완료된 것입니다. 🎉";
    }
}

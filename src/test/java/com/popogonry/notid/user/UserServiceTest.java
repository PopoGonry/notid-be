package com.popogonry.notid.user;

import com.popogonry.notid.global.jwt.JwtAuthenticationFilter;
import com.popogonry.notid.global.jwt.JwtTokenProvider;
import com.popogonry.notid.user.dto.UserSignInRequest;
import com.popogonry.notid.user.dto.UserSignUpRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Test
    public void 회원가입() throws Exception {
        //given
        String rawPassword = "password1234";
        UserSignUpRequest userSignUpRequest = new UserSignUpRequest("test@gmail.com", rawPassword, "name", Gender.ETC, "010-0000-0000", null);

        //when
        Long userId = userService.signUp(userSignUpRequest);

        //then
        User savedUser = userRepository.findById(userId).orElseThrow();

        // 저장 확인
        assertNotNull(savedUser.getId());
        assertEquals("test@gmail.com", savedUser.getEmail());

        // 비밀번호 암호화 확인
        assertNotEquals(rawPassword, savedUser.getPassword());

        // 암호화 2차 확인
        assertTrue(passwordEncoder.matches(rawPassword, savedUser.getPassword()));
    }

    @Test
    public void 회원가입_이메일_중복검증() throws Exception {
        //given
        UserSignUpRequest userSignUpRequest = new UserSignUpRequest("test@gmail.com", "password", "name", Gender.ETC, "010-0000-0000", null);
        userService.signUp(userSignUpRequest);

        UserSignUpRequest sameEmail = new UserSignUpRequest("test@gmail.com", "password", "name", Gender.ETC, "010-3333-4444", null);

        //when
        //then
        Assertions.assertThatThrownBy(() -> userService.signUp(sameEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 이메일입니다.");
    }

    @Test
    public void 회원가입_연락처_중복검증() throws Exception {
        //given
        UserSignUpRequest userSignUpRequest = new UserSignUpRequest("test@gmail.com", "password", "name", Gender.ETC, "010-0000-0000", null);
        userService.signUp(userSignUpRequest);

        UserSignUpRequest samePhone = new UserSignUpRequest("test2@gmail.com", "password", "name", Gender.ETC, "010-0000-0000", null);

        //when
        //then
        Assertions.assertThatThrownBy(() -> userService.signUp(samePhone))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 연락처입니다.");
    }

    @Test
    public void 로그인() throws Exception {
        //given
        String email = "test@gmail.com";
        String password = "password";
        UserSignUpRequest userSignUpRequest = new UserSignUpRequest(email, password, "name", Gender.ETC, "010-0000-0000", null);
        userService.signUp(userSignUpRequest);

        UserSignInRequest userSignInRequest = new UserSignInRequest(email, password);

        //when
        String token = userService.signIn(userSignInRequest);

        //then
        assertNotNull(token);
    }

    @Test
    public void 로그인_JWT검증() throws Exception {
        //given
        String email = "test@gmail.com";
        String password = "password";
        UserSignUpRequest userSignUpRequest = new UserSignUpRequest(email, password, "name", Gender.ETC, "010-0000-0000", null);
        userService.signUp(userSignUpRequest);

        UserSignInRequest userSignInRequest = new UserSignInRequest(email, password);
        String token = userService.signIn(userSignInRequest);

        //when
        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        String tokenEmail = authentication.getName();

        //then
        assertEquals(email, tokenEmail);
    }

    @Test
    public void 로그인_실패_비밀번호_틀림() throws Exception {
        //given
        String email = "test@gmail.com";
        String password = "password";
        UserSignUpRequest userSignUpRequest = new UserSignUpRequest(email, password, "name", Gender.ETC, "010-0000-0000", null);
        userService.signUp(userSignUpRequest);

        //when
        UserSignInRequest wrongPasswordRequest = new UserSignInRequest(email, "wrongPassword");

        //then
        Assertions.assertThatThrownBy(() -> userService.signIn(wrongPasswordRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
    }

    @Test
    public void 로그인_실패_없는_이메일() throws Exception {
        //given
        // 가입을 안 시킴 (DB가 비어있음)
        UserSignInRequest noUserRequest = new UserSignInRequest("ghost@gmail.com", "password");

        //when & then
        Assertions.assertThatThrownBy(() -> userService.signIn(noUserRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("가입되지 않은 이메일입니다."); // UserService에 적은 메시지와 똑같아야 함!
    }
}
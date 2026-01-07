package com.popogonry.notid.user;

import com.popogonry.notid.channeluser.ChannelUser;
import com.popogonry.notid.channeluser.ChannelUserRepository;
import com.popogonry.notid.global.jwt.JwtAuthenticationFilter;
import com.popogonry.notid.global.jwt.JwtTokenProvider;
import com.popogonry.notid.user.dto.*;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;

import static org.assertj.core.api.Assertions.*;
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

    @Autowired
    EntityManager em;
    @Autowired
    private RequestAttributes requestAttributes;
    private ChannelUserRepository channelUserRepository;

    @Test
    public void 회원가입() throws Exception {
        //given
        String rawPassword = "password1234";
        UserSignUpRequest userSignUpRequest = new UserSignUpRequest("test@gmail.com", rawPassword, rawPassword, "name", Gender.ETC, "010-0000-0000", null);

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
        User user = userRepository.findById(simpleSignUp()).orElseThrow();

        UserSignUpRequest sameEmail = new UserSignUpRequest(user.getEmail(), "password", "password", "name", Gender.ETC, "010-3333-4444", null);

        //when
        //then
        assertThatThrownBy(() -> userService.signUp(sameEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 이메일입니다.");
    }

    @Test
    public void 회원가입_연락처_중복검증() throws Exception {
        //given
        User user = userRepository.findById(simpleSignUp()).orElseThrow();

        UserSignUpRequest samePhone = new UserSignUpRequest("test2@gmail.com", "password", "password", "name", Gender.ETC, user.getPhone(), null);

        //when
        //then
        assertThatThrownBy(() -> userService.signUp(samePhone))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 연락처입니다.");
    }

    @Test
    public void 로그인() throws Exception {
        //given
        User user = userRepository.findById(simpleSignUp()).orElseThrow();

        UserSignInRequest userSignInRequest = new UserSignInRequest(user.getEmail(), "password");

        //when
        String token = userService.signIn(userSignInRequest);

        //then
        assertNotNull(token);
    }

    @Test
    public void 로그인_JWT검증() throws Exception {
        //given
        User user = userRepository.findById(simpleSignUp()).orElseThrow();

        UserSignInRequest userSignInRequest = new UserSignInRequest(user.getEmail(), "password");
        String token = userService.signIn(userSignInRequest);

        //when
        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        String tokenEmail = authentication.getName();

        //then
        assertEquals(user.getEmail(), tokenEmail);
    }

    @Test
    public void 로그인_실패_비밀번호_틀림() throws Exception {
        //given
        String email = userRepository.findById(simpleSignUp()).orElseThrow().getEmail();

        //when
        UserSignInRequest wrongPasswordRequest = new UserSignInRequest(email, "wrongPassword");

        //then
        assertThatThrownBy(() -> userService.signIn(wrongPasswordRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
    }

    @Test
    public void 로그인_실패_없는_이메일() throws Exception {
        //given
        // 가입을 안 시킴 (DB가 비어있음)
        UserSignInRequest noUserRequest = new UserSignInRequest("ghost@gmail.com", "password");

        //when & then
        assertThatThrownBy(() -> userService.signIn(noUserRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("가입되지 않은 이메일입니다."); // UserService에 적은 메시지와 똑같아야 함!
    }

    @Test
    public void 사용자_정보_수정() throws Exception {
        //given
        User user = userRepository.findById(simpleSignUp()).orElseThrow();

        UserUpdateRequest userUpdateRequest = new UserUpdateRequest("changeName", Gender.FEMALE, "010-1234-5678", null);

        //when
        userService.updateInfo(user.getId(), userUpdateRequest, user.getEmail());

        em.flush();
        em.clear();

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();

        //then
        assertEquals("changeName", updatedUser.getName());
        assertEquals(Gender.FEMALE, updatedUser.getGender());
        assertEquals("010-1234-5678", updatedUser.getPhone());
    }

    @Test
    public void 사용자_정보_수정_실패_권한없음() throws Exception {
        //given
        User user = userRepository.findById(simpleSignUp()).orElseThrow();

        String hackerEmail = "hacker@gmail.com";
        UserUpdateRequest request = new UserUpdateRequest("hackedName", Gender.MALE, "010-6666-6666", null);

        //when
        //then
        assertThatThrownBy(() -> userService.updateInfo(user.getId(), request, hackerEmail))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("본인의 정보만 수정할 수 있습니다.");

    }

    @Test
    public void 사용자_비밀번호_수정() throws Exception {
        //given
        User user = userRepository.findById(simpleSignUp()).orElseThrow();

        String newPassword = "newPassword";
        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("password", newPassword, newPassword);

        //when
        userService.updatePassword(user.getId(), request, user.getEmail());

        em.flush();
        em.clear();

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();

        //then
        assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPassword()));
    }

    @Test
    public void 사용자_비밀번호_수정_실패_비밀번호틀림() throws Exception {
        //given
        User user = userRepository.findById(simpleSignUp()).orElseThrow();

        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("wrongPassword", "newPassword", "newPassword");

        //when
        //then
        assertThatThrownBy(() -> userService.updatePassword(user.getId(), request, user.getEmail()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
    }

    @Test
    public void 사용자_비밀번호_수정_실패_권한없음() throws Exception {
        //given
        User user = userRepository.findById(simpleSignUp()).orElseThrow();

        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("password", "newPassword", "newPassword");

        //when
        //then
        assertThatThrownBy(() -> userService.updatePassword(user.getId() + 1L, request, user.getEmail()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("본인의 비밀번호만 변경할 수 있습니다.");
    }
    @Test
    public void 사용자_비밀번호_수정_실패_확인비밀번호다름() throws Exception {
        //given
        User user = userRepository.findById(simpleSignUp()).orElseThrow();

        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("password", "newPassword", "diffPassword");

        //when
        //then
        assertThatThrownBy(() -> userService.updatePassword(user.getId(), request, user.getEmail()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
    }

    @Test
    public void 사용자_비밀번호_수정_실패_기존비밀번호동일변경() throws Exception {
        //given
        User user = userRepository.findById(simpleSignUp()).orElseThrow();

        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("password", "password", "password");

        //when
        //then
        assertThatThrownBy(() -> userService.updatePassword(user.getId(), request, user.getEmail()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("기존 비밀번호와 동일하게 변경할 수 없습니다.");
    }

    @Test
    public void 사용자_탈퇴() throws Exception {
        //given
        User user = userRepository.findById(simpleSignUp()).orElseThrow();

        UserWithdrawRequest request = new UserWithdrawRequest("password", "password");

        //when
        userService.withdraw(user.getId(), request,  user.getEmail());


        //then

    }





    private Long simpleSignUp() {
        UserSignUpRequest userSignUpRequest = new UserSignUpRequest("test@gmail.com", "password", "password", "name", Gender.ETC, "010-0000-0000", null);
        return userService.signUp(userSignUpRequest);
    }
}
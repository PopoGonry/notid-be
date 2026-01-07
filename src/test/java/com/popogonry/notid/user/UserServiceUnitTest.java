package com.popogonry.notid.user;


import com.popogonry.notid.channeluser.ChannelUserRepository;
import com.popogonry.notid.global.jwt.JwtTokenProvider;
import com.popogonry.notid.organizationuser.OrganizationUserRepository;
import com.popogonry.notid.user.dto.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

    @InjectMocks
    private UserService userService;

    @Mock
    UserRepository userRepository;

    @Mock
    OrganizationUserRepository organizationUserRepository;

    @Mock
    ChannelUserRepository channelUserRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    EntityManager em;

    Long userId;
    String email;
    String password;

    User user;

    @BeforeEach
    void setUp() {
        userId = 1L;
        email = "test@gmail.com";
        password = "password";

        user = User.builder()
                .email(email)
                .password("encodedPassword")
                .name("name")
                .gender(Gender.ETC)
                .phone("010-0000-0000")
                .build();

        ReflectionTestUtils.setField(user, "id", userId);
    }

    @Test
    @DisplayName("사용자 회원가입 성공")
    public void signUp_success() throws Exception {
        //given
        UserSignUpRequest request = new UserSignUpRequest(email, password, password, "name", Gender.ETC, "010-0000-0000", null);

        //Mocking
        given(userRepository.existsByEmail(any())).willReturn(false);
        given(userRepository.existsByPhone(any())).willReturn(false);
        given(passwordEncoder.encode(password)).willReturn("encodedPassword");

        given(userRepository.save(any(User.class))).willReturn(user);

        //when
        Long savedId = userService.signUp(request);

        //then
        assertThat(savedId).isEqualTo(userId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 회원가입 실패 - 이메일 중복")
    public void signUp_fail_duplicate_email() throws Exception {
        //given
        UserSignUpRequest request = new UserSignUpRequest(email, password, password, "name", Gender.ETC, "010-0000-0000", null);

        //when
        given(userRepository.existsByEmail(any())).willReturn(true);

        //then
        assertThatThrownBy(() -> userService.signUp(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 이메일입니다.");
    }

    @Test
    @DisplayName("사용자 회원가입 실패 - 연락처 중복")
    public void signUp_fail_duplicate_phone() throws Exception {
        //given
        UserSignUpRequest request = new UserSignUpRequest(email, password, password, "name", Gender.ETC, "010-0000-0000", null);
        
        //when
        given(userRepository.existsByPhone(any())).willReturn(true);
        
        //then
        assertThatThrownBy(() -> userService.signUp(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 연락처입니다.");
    }
    
    @Test
    @DisplayName("사용자 회원가입 실패 - 확인 비밀번호 다름")
    public void signUp_fail_different_check_password() throws Exception {
        //given
        UserSignUpRequest request = new UserSignUpRequest(email, password, "wrong" + password, "name", Gender.ETC, "010-0000-0000", null);

        //when

        //then
        assertThatThrownBy(() -> userService.signUp(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("사용자 로그인 성공")
    public void signIn_success() throws Exception {
        //given
        UserSignInRequest request = new UserSignInRequest(email, password);
        String expectedToken = "accessToken";

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(password, user.getPassword())).willReturn(true);
        given(jwtTokenProvider.createToken(email)).willReturn(expectedToken);

        //when
        String token = userService.signIn(request);

        //then
        assertThat(token).isEqualTo(expectedToken);
    }

    @Test
    @DisplayName("사용자 로그인 실패 - 틀린 비밀번호")
    public void signIn_fail_wrong_password() throws Exception {
        //given
        UserSignInRequest request = new UserSignInRequest(email, "wrong" + password);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong" + password, user.getPassword())).willReturn(false);

        //when
        //then
        assertThatThrownBy(() -> userService.signIn(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("사용자 로그인 실패 - 탈퇴한 사용자")
    public void signIn_fail_withdrawn_user() throws Exception {
        //given
        UserSignInRequest request = new UserSignInRequest(email, password);
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        //when
        ReflectionTestUtils.setField(user, "status", UserStatus.WITHDRAWN);

        //then
        assertThatThrownBy(() -> userService.signIn(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("탈퇴한 계정입니다.");
    }

    @Test
    @DisplayName("사용자 정보 수정 성공")
    public void updateInfo_success() throws Exception {
        //given
        UserUpdateRequest request = new UserUpdateRequest("changeName", Gender.FEMALE, "010-1234-5678", null);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        //when
        userService.updateInfo(userId, request, email);

        //then
        assertThat(user.getName()).isEqualTo("changeName");
        assertThat(user.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(user.getPhone()).isEqualTo("010-1234-5678");

        verify(organizationUserRepository, times(1)).deleteAllByUserId(userId);
    }

    @Test
    @DisplayName("사용자 정보 수정 실패 - 권한 없음")
    public void updateInfo_fail_access_denied() throws Exception {
        //given
        UserUpdateRequest request = new UserUpdateRequest("changeName", Gender.FEMALE, "010-1234-5678", null);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        //when
        //then
        assertThatThrownBy(() -> userService.updateInfo(userId, request, "wrong" + email))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("본인의 계정에만 접근할 수 있습니다.");
    }

    @Test
    @DisplayName("사용자 비밀번호 수정 성공")
    public void updatePassword_success() throws Exception {
        //given
        String newPassword = "newPassword";
        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest(password, newPassword, newPassword);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(password, user.getPassword())).willReturn(true);
        given(passwordEncoder.encode(newPassword)).willReturn("newEncodedPassword");

        //when
        userService.updatePassword(userId, request, email);

        //then
        assertThat(user.getPassword()).isEqualTo("newEncodedPassword");
    }

    @Test
    @DisplayName("사용자 비밀번호 수정 실패 - 권한 없음")
    public void updatePassword_fail_access_denied() throws Exception {
        //given
        String newPassword = "newPassword";
        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest(password, newPassword, newPassword);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        //when
        //then
        assertThatThrownBy(() -> userService.updatePassword(userId, request, "wrong" + email))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("본인의 계정에만 접근할 수 있습니다.");
    }

    @Test
    @DisplayName("사용자 비밀번호 수정 실패 - 틀린 비밀번호")
    public void updatePassword_fail_wrong_password() throws Exception {
        //given
        String newPassword = "newPassword";
        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("wrong" + password, newPassword, newPassword);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong" + password, user.getPassword())).willReturn(false);

        //when
        //then
        assertThatThrownBy(() -> userService.updatePassword(userId, request, email))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("사용자 비밀번호 수정 실패 - 확인 비밀번호 다름")
    public void updatePassword_fail_different_check_password() throws Exception {
        //given
        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest(password, "newPassword", "diffNewPassword");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(password, user.getPassword())).willReturn(true);

        //when
        //then
        assertThatThrownBy(() -> userService.updatePassword(userId, request, email))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("사용자 비밀번호 수정 실패 - 기존 비밀번호와 같음")
    public void updatePassword_fail_old_password_same() throws Exception {
        //given
        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest(password, password, password);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(password, user.getPassword())).willReturn(true);

        //when
        //then
        assertThatThrownBy(() -> userService.updatePassword(userId, request, email))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("기존 비밀번호와 동일하게 변경할 수 없습니다.");
    }


    @Test
    @DisplayName("사용자 탈퇴 성공")
    public void withdraw_success() throws Exception {
        //given
        UserWithdrawRequest request = new UserWithdrawRequest(password, password);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        given(passwordEncoder.matches(password, user.getPassword())).willReturn(true);

        //when
        userService.withdraw(userId, request,  user.getEmail());

        //then
        assertEquals(UserStatus.WITHDRAWN, user.getStatus());

        verify(organizationUserRepository, times(1)).deleteAllByUserId(userId);
        verify(channelUserRepository, times(1)).deleteAllByUserId(userId);

        verify(em).flush();
        verify(em).clear();
    }
    
    @Test
    @DisplayName("사용자 탈퇴 실패 - 권한 없음")
    public void withdraw_fail_access_denied() throws Exception {
        //given
        UserWithdrawRequest request = new UserWithdrawRequest(password, password);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        //when
        //then
        assertThatThrownBy(() -> userService.withdraw(userId, request, "wrong" + email))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("본인의 계정에만 접근할 수 있습니다.");
    }

    @Test
    @DisplayName("사용자 탈퇴 실패 - 틀린 비밀번호")
    public void withdraw_fail_wrong_password() throws Exception {
        //given
        UserWithdrawRequest request = new UserWithdrawRequest("wrong" + password, "wrong" + password);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong" + password, user.getPassword())).willReturn(false);

        //when
        //then
        assertThatThrownBy(() -> userService.withdraw(userId, request, email))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
    }
    
    @Test
    @DisplayName("사용자 탈퇴 실패 - 확인 비밀번호 다름")
    public void withdraw_fail_different_check_password() throws Exception {
        //given
        UserWithdrawRequest request = new UserWithdrawRequest(password, "wrong" + password);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(password, user.getPassword())).willReturn(true);

        //when
        //then
        assertThatThrownBy(() -> userService.withdraw(userId, request, email))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호와 확인 비밀번호가 일치하지 않습니다.");
    }
}

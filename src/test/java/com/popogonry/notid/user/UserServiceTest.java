package com.popogonry.notid.user;

import com.popogonry.notid.user.dto.UserSignUpRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

    @Test
    public void 회원가입() throws Exception {
        //given
        String rawPassword = "password1234";
        UserSignUpRequest userSignUpRequest = new UserSignUpRequest("test@gmail.com", rawPassword, "name");

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
}
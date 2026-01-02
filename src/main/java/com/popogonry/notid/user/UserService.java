package com.popogonry.notid.user;

import com.popogonry.notid.global.jwt.JwtTokenProvider;
import com.popogonry.notid.organizationchannel.OrganizationChannel;
import com.popogonry.notid.organizationuser.OrganizationUser;
import com.popogonry.notid.organizationuser.OrganizationUserRepository;
import com.popogonry.notid.user.dto.UserResponse;
import com.popogonry.notid.user.dto.UserSignInRequest;
import com.popogonry.notid.user.dto.UserSignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public Long signUp(UserSignUpRequest request) {
        // 예외 처리
        validateDuplicateUser(request);

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = request.toEntity(encodedPassword);
        return userRepository.save(user).getId();
    }

    private void validateDuplicateUser(UserSignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("이미 존재하는 연락처입니다.");
        }
    }

    public String signIn(UserSignInRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return jwtTokenProvider.createToken(user.getEmail());
    }
}

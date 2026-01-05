package com.popogonry.notid.user;

import com.popogonry.notid.global.jwt.JwtTokenProvider;
import com.popogonry.notid.organization.Organization;
import com.popogonry.notid.organization.OrganizationRepository;
import com.popogonry.notid.organizationchannel.OrganizationChannel;
import com.popogonry.notid.organizationuser.OrganizationUser;
import com.popogonry.notid.organizationuser.OrganizationUserRepository;
import com.popogonry.notid.user.dto.UserResponse;
import com.popogonry.notid.user.dto.UserSignInRequest;
import com.popogonry.notid.user.dto.UserSignUpRequest;
import com.popogonry.notid.user.dto.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final OrganizationRepository organizationRepository;
    private final OrganizationUserRepository organizationUserRepository;

    @Transactional
    public Long signUp(UserSignUpRequest request) {
        // 예외 처리
        validateDuplicateUser(request);

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User savedUser = userRepository.save(request.toEntity(encodedPassword));

        List<Long> orgIds = request.getOrganizationIds();
        organizationUserSet(orgIds, savedUser);
        return savedUser.getId();
    }

    private void organizationUserSet(List<Long> orgIds, User savedUser) {
        if(orgIds != null && !orgIds.isEmpty()) {

            Set<Long> uniqueOrgIds = new HashSet<>(orgIds);

            List<Organization> orgs = organizationRepository.findAllById(uniqueOrgIds);

            if(orgs.size() != uniqueOrgIds.size()) {
                throw new IllegalArgumentException("존재하지 않는 조직 ID가 포함되어 있습니다.");
            }

            List<OrganizationUser> orgUsers = orgs.stream()
                    .map(org -> new OrganizationUser(savedUser, org))
                    .toList();

            organizationUserRepository.saveAll(orgUsers);
        }
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


    @Transactional
    public Long updateInfo(Long userId, UserUpdateRequest request, String tokenEmail) throws AccessDeniedException {

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("로그인 유저 정보를 찾을 수 없습니다."));

        if(!targetUser.getEmail().equals(tokenEmail)) {
            throw new org.springframework.security.access.AccessDeniedException("본인의 정보만 수정할 수 있습니다.");
        }

        organizationUserRepository.deleteAllByUser(targetUser);
        organizationUserSet(request.getOrganizationIds(), targetUser);

        targetUser.updateInfo(request.getName(), request.getGender(), request.getPhone());

        return targetUser.getId();
    }

    // 토큰에서 값 꺼내와서, 본인 것만 수정 가능한 방식
//    @Transactional
//    public Long updateInfo(UserUpdateRequest request, String email) {
//
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new IllegalArgumentException("로그인 유저 정보를 찾을 수 없습니다."));
//
//        organizationUserRepository.deleteAllByUser(user);
//        organizationUserSet(request.getOrganizationIds(), user);
//
//        user.updateInfo(request.getName(), request.getGender(), request.getPhone());
//
//        return user.getId();
//    }


}

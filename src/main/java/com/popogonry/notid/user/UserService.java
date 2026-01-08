package com.popogonry.notid.user;

import com.popogonry.notid.channeluser.ChannelUserRepository;
import com.popogonry.notid.global.jwt.JwtTokenProvider;
import com.popogonry.notid.organization.Organization;
import com.popogonry.notid.organization.OrganizationRepository;
import com.popogonry.notid.organizationuser.OrganizationUser;
import com.popogonry.notid.organizationuser.OrganizationUserRepository;
import com.popogonry.notid.user.dto.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
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
    private final ChannelUserRepository channelUserRepository;
    private final EntityManager em;

    @Transactional
    public Long signUp(UserSignUpRequest request) {
        // 예외 처리
        validateDuplicateUser(request);

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User savedUser = userRepository.save(request.toEntity(encodedPassword));

        organizationUserSet(request.getOrganizationIds(), savedUser);
        return savedUser.getId();
    }

    private void organizationUserSet(List<Long> orgIds, User user) {
        if(orgIds != null && !orgIds.isEmpty()) {

            Set<Long> uniqueOrgIds = new HashSet<>(orgIds);

            List<Organization> orgs = organizationRepository.findAllById(uniqueOrgIds);

            if(orgs.size() != uniqueOrgIds.size()) {
                throw new IllegalArgumentException("존재하지 않는 조직 ID가 포함되어 있습니다.");
            }

            List<OrganizationUser> orgUsers = orgs.stream()
                    .map(org -> new OrganizationUser(user, org))
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
        if (!request.isNewPasswordSame()) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
    }

    public String signIn(UserSignInRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        if (user.getStatus() == UserStatus.WITHDRAWN) {
            throw new IllegalArgumentException("탈퇴한 계정입니다.");
        }

        checkPassword(request.getPassword(), user);

        return jwtTokenProvider.createToken(user.getEmail());
    }

    private void checkPassword(String requestPassword, User user) {
        if(!passwordEncoder.matches(requestPassword, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
    }

    public User getUser(Long userId, String tokenEmail) {
        User user = getUserById(userId);
        validateOwner(user, tokenEmail);

        return user;
    }


    @Transactional
    public Long updateInfo(Long userId, UserUpdateRequest request, String tokenEmail) {

        User targetUser = getUserById(userId);

        validateOwner(targetUser, tokenEmail);

        targetUser.updateInfo(request.getName(), request.getGender(), request.getPhone());

        organizationUserRepository.deleteAllByUserId(targetUser.getId());
        organizationUserSet(request.getOrganizationIds(), targetUser);

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

    @Transactional
    public Long updatePassword(Long userId, UserPasswordUpdateRequest request, String tokenEmail) {
        User user = getUserById(userId);

        validateOwner(user, tokenEmail);

        checkPassword(request.getOldPassword(), user);

        if(!request.isNewPasswordSame()) {
            throw new IllegalArgumentException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        if(passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("기존 비밀번호와 동일하게 변경할 수 없습니다.");
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));

        return user.getId();
    }

    private static void validateOwner(User user, String tokenEmail) {
        if (!user.getEmail().equals(tokenEmail)) {
            throw new AccessDeniedException("본인의 계정에만 접근할 수 있습니다.");
        }
    }

    @Transactional
    public Long withdraw(Long userId, UserWithdrawRequest request, String tokenEmail) {
        User user = getUserById(userId);

        validateOwner(user, tokenEmail);

        checkPassword(request.getPassword(), user);

        if(!request.isPasswordSame()) {
            throw new IllegalArgumentException("비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        user.withdraw();

        em.flush();

        organizationUserRepository.deleteAllByUserId(user.getId());
        channelUserRepository.deleteAllByUserId(user.getId());

        em.clear();
        return userId;
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));
    }
}

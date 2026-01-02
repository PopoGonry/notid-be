package com.popogonry.notid.organizationuser;

import com.popogonry.notid.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrganizationUserRepository extends JpaRepository<OrganizationUser, Long> {
    List<OrganizationUser> findByUser(User user);

    void deleteAllByUser(User user);
}

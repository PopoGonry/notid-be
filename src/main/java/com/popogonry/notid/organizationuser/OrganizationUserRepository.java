package com.popogonry.notid.organizationuser;

import com.popogonry.notid.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationUserRepository extends JpaRepository<OrganizationUser, Long> {
    List<OrganizationUser> findByUser(User user);

    @Modifying
    @Query("delete from OrganizationUser ou where ou.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}

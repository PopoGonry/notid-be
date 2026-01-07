package com.popogonry.notid.channeluser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChannelUserRepository extends JpaRepository<ChannelUser, Long> {
    @Modifying
    @Query("delete from ChannelUser cu where cu.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}

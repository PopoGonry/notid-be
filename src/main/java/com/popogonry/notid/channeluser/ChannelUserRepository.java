package com.popogonry.notid.channeluser;

import com.popogonry.notid.channel.Channel;
import com.popogonry.notid.user.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChannelUserRepository extends JpaRepository<ChannelUser, Long> {
    @Modifying
    @Query("delete from ChannelUser cu where cu.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    Optional<ChannelUser> findByChannelIdAndUserId(Long channelId, Long userId);

    Page<ChannelUser> findAllByChannelId(Long channelId, Pageable pageable);


    long countByChannelIdAndChannelGrade(Long channelId, ChannelGrade channelGrade);
}

package com.popogonry.notid.channel.dto;

import com.popogonry.notid.channel.Channel;
import com.popogonry.notid.channeluser.ChannelGrade;
import com.popogonry.notid.channeluser.ChannelUser;
import com.popogonry.notid.user.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelMemberResponse {
    private String email;

    private String name;

    private Gender gender;

    private String phone;

    private ChannelGrade channelGrade;

    private LocalDateTime createdAt;

    public static ChannelMemberResponse from(ChannelUser channelUser) {
        return ChannelMemberResponse.builder()
                .email(channelUser.getUser().getEmail())
                .name(channelUser.getUser().getName())
                .gender(channelUser.getUser().getGender())
                .phone(channelUser.getUser().getPhone())
                .channelGrade(channelUser.getChannelGrade())
                .createdAt(channelUser.getCreatedAt())
                .build();
    }
}

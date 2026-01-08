package com.popogonry.notid.channel.dto;

import com.popogonry.notid.channel.Channel;
import com.popogonry.notid.channel.ChannelStatus;
import com.popogonry.notid.channel.JoinType;
import com.popogonry.notid.user.Gender;
import com.popogonry.notid.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChannelCreateRequest {

    @NotBlank(message = "채널 이름은 필수입니다.")
    @Column(nullable = false)
    private String name;

    private String description;

    private JoinType joinType;

    private List<Long> organizationIds = new ArrayList<>();

    public Channel toEntity() {
        return Channel.builder()
                .name(name)
                .description(description)
                .joinType(joinType)
                .build();
    }
}

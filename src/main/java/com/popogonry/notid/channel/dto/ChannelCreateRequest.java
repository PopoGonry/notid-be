package com.popogonry.notid.channel.dto;

import com.popogonry.notid.channel.Channel;
import com.popogonry.notid.channel.JoinType;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
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

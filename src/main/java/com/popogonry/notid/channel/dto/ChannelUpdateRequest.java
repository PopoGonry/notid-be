package com.popogonry.notid.channel.dto;

import com.popogonry.notid.channel.JoinType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChannelUpdateRequest {
    private String description;

    private JoinType joinType = JoinType.FREE;

    private List<Long> organizationIds = new ArrayList<>();
}

package com.popogonry.notid.channel.dto;

import com.popogonry.notid.channel.Channel;
import com.popogonry.notid.channel.JoinType;
import com.popogonry.notid.organizationchannel.OrganizationChannel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChannelResponse {
    private Long id;
    private String name;
    private String description;
    private JoinType joinType = JoinType.FREE;
    private LocalDateTime createdAt;
    private List<OrganizationInfo> organizationChannels = new ArrayList<>();

    public static ChannelResponse from(Channel channel) {
        return new ChannelResponse(
                channel.getId(),
                channel.getName(),
                channel.getDescription(),
                channel.getJoinType(),
                channel.getCreatedAt(),

                // ★ 핵심: 엔티티 리스트(OrganizationChannel) -> DTO 리스트(OrganizationInfo) 변환
                // "채널에 연결된 조직 채널들을 돌면서 -> 그 안의 조직 정보를 꺼내서 -> DTO로 만듦"
                channel.getOrganizationChannels().stream()
                        .map(oc -> new OrganizationInfo(
                                oc.getOrganization().getId(),
                                oc.getOrganization().getName())
                        )
                        .toList()
        );
    }

    @Getter
    @AllArgsConstructor
    public static class OrganizationInfo {
        private Long id;
        private String name;
    }
}

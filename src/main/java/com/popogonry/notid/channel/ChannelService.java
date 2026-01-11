package com.popogonry.notid.channel;

import com.popogonry.notid.channel.dto.ChannelCreateRequest;
import com.popogonry.notid.channel.dto.ChannelResponse;
import com.popogonry.notid.channel.dto.ChannelSearchRequest;
import com.popogonry.notid.channel.dto.ChannelUpdateRequest;
import com.popogonry.notid.channeluser.ChannelGrade;
import com.popogonry.notid.channeluser.ChannelUser;
import com.popogonry.notid.channeluser.ChannelUserRepository;
import com.popogonry.notid.organization.Organization;
import com.popogonry.notid.organization.OrganizationRepository;
import com.popogonry.notid.organizationchannel.OrganizationChannel;
import com.popogonry.notid.organizationchannel.OrganizationChannelRepository;
import com.popogonry.notid.user.User;
import com.popogonry.notid.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final ChannelUserRepository channelUserRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationChannelRepository organizationChannelRepository;

    @Transactional
    public Long createChannel(ChannelCreateRequest request, String tokenEmail) {

        User user = getUser(tokenEmail);

        Channel channel = channelRepository.save(request.toEntity());

        channelUserRepository.save(new ChannelUser(channel, user, ChannelGrade.ADMIN));

        organizationChannelSet(request.getOrganizationIds(), channel);

        return channel.getId();
    }

    private void organizationChannelSet(List<Long> orgIds, Channel channel) {
        if(orgIds != null && !orgIds.isEmpty()) {

            Set<Long> uniqueOrgIds = new HashSet<>(orgIds);

            List<Organization> orgs = organizationRepository.findAllById(uniqueOrgIds);

            if(orgs.size() != uniqueOrgIds.size()) {
                throw new IllegalArgumentException("존재하지 않는 조직 ID가 포함되어 있습니다.");
            }

            List<OrganizationChannel> orgChannels = orgs.stream()
                    .map(org -> new OrganizationChannel(channel, org))
                    .toList();

            organizationChannelRepository.saveAll(orgChannels);
        }
    }

    public Page<ChannelResponse> searchChannels(ChannelSearchRequest request, String tokenEmail, Pageable pageable) {
        getUser(tokenEmail);

        String keyword = request.getKeyword();

        Page<Channel> channelResponses = switch (request.getSearchType()) {
            case ID -> searchById(keyword, pageable);
            case NAME -> channelRepository.findByNameContaining(keyword, pageable);
            case DESCRIPTION -> channelRepository.findByDescriptionContaining(keyword, pageable);
            case ORGANIZATION -> channelRepository.findByOrganizationName(keyword, pageable);
        };
        return channelResponses.map(ChannelResponse::from);
    }

    public Page<Channel> searchById(String keyword, Pageable pageable) {
        try {
            Long id = Long.parseLong(keyword);

            Optional<Channel> channelOptional = channelRepository.findById(id);

            if (channelOptional.isPresent()) {
                return new PageImpl<>(List.of(channelOptional.get()), pageable, 1);
            }

            return Page.empty();

        } catch (NumberFormatException e) {
            return Page.empty();
        }
    }

    @Transactional
    public Long updateChannel(Long channelId, ChannelUpdateRequest request, String tokenEmail) {
        Channel channel = getChannel(channelId);
        User user = getUser(tokenEmail);

        ChannelUser channelUser = getChannelUser(channel, user);

        if (channelUser.getChannelGrade() != ChannelGrade.ADMIN) {
            throw new AccessDeniedException("관리자 권한이 필요합니다.");
        }

        channel.updateChannel(request.getDescription(), request.getJoinType());

        updateOrganizationRelations(request.getOrganizationIds(), channel);

        return channel.getId();
    }


    private void updateOrganizationRelations(List<Long> requestOrgIds, Channel channel) {
        List<OrganizationChannel> currentLinks = channel.getOrganizationChannels();

        // 기존 목록 ID 추출
        List<Long> currentOrgIds = currentLinks.stream()
                .map(link -> link.getOrganization().getId())
                .toList();

        // 기존 목록에서 요청 목록 제외 == 제거할 것.
        List<OrganizationChannel> toRemove = currentLinks.stream()
                .filter(link -> !requestOrgIds.contains(link.getOrganization().getId()))
                .toList();

        toRemove.forEach(channel::removeOrganizationChannel);

        // 요청 목록에서 기존 목록 제외 == 추가할 것.
        List<Long> toAdd = requestOrgIds.stream()
                .filter(id -> !currentOrgIds.contains(id))
                .toList();

        if (!toAdd.isEmpty()) {
            List<Organization> newOrgs = organizationRepository.findAllById(toAdd);

            if (newOrgs.size() != toAdd.size()) {
                throw new IllegalArgumentException("존재하지 않는 조직 ID가 포함되어 있습니다.");
            }

            for (Organization org : newOrgs) {
                channel.addOrganizationChannel(new OrganizationChannel(channel, org));
            }
        }
    }

    @Transactional
    public Long deleteChannel(Long channelId, String tokenEmail) {
        Channel channel = getChannel(channelId);

        if (channel.getStatus() == ChannelStatus.INACTIVE) {
            throw new IllegalArgumentException("이미 삭제된 채널입니다.");
        }

        User user = getUser(tokenEmail);

        ChannelUser channelUser = getChannelUser(channel, user);

        if (channelUser.getChannelGrade() != ChannelGrade.ADMIN) {
            throw new AccessDeniedException("관리자 권한이 필요합니다.");
        }

        channel.inactive();
        return channel.getId();
    }

    private ChannelUser getChannelUser(Channel channel, User user) {
        return channelUserRepository.findByChannelIdAndUserId(channel.getId(), user.getId())
                .orElseThrow(() -> new IllegalArgumentException("채널에 가입되지 않은 사용자입니다."));
    }

    private Channel getChannel(Long channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("채널 정보를 찾을 수 없습니다."));
    }

    private User getUser(String tokenEmail) {
        return userRepository.findByEmail(tokenEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));
    }

}


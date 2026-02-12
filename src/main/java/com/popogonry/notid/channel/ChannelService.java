package com.popogonry.notid.channel;

import com.popogonry.notid.channel.dto.*;
import com.popogonry.notid.channeluser.ChannelGrade;
import com.popogonry.notid.channeluser.ChannelUser;
import com.popogonry.notid.channeluser.ChannelUserRepository;
import com.popogonry.notid.organization.Organization;
import com.popogonry.notid.organization.OrganizationRepository;
import com.popogonry.notid.organizationchannel.OrganizationChannel;
import com.popogonry.notid.organizationchannel.OrganizationChannelRepository;
import com.popogonry.notid.user.User;
import com.popogonry.notid.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
    private final UserService userService;
    private final OrganizationRepository organizationRepository;
    private final OrganizationChannelRepository organizationChannelRepository;

    @Transactional
    public Long createChannel(ChannelCreateRequest request, String userEmail) {

        User user = userService.getUser(userEmail);

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

    public Page<ChannelResponse> searchChannels(ChannelSearchRequest request, Pageable pageable) {
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
    public Long updateChannel(Long channelId, ChannelUpdateRequest request, String userEmail) {
        Channel channel = getChannel(channelId);
        validateChannelAdmin(userEmail, channel);

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
    public Long deleteChannel(Long channelId, String userEmail) {
        Channel channel = validateChannel(channelId);

        validateChannelAdmin(userEmail, channel);

        channel.inactive();
        return channel.getId();
    }

    @Transactional
    public Long joinChannel(Long channelId, String userEmail) {
        Channel channel = validateChannel(channelId);

        User user = userService.getUser(userEmail);

        if (channelUserRepository.findByChannelIdAndUserId(channelId, user.getId()).isPresent()) {
            throw new IllegalArgumentException("이미 가입되었거나 가입 승인 대기 중입니다.");
        }

        ChannelGrade grade = ChannelGrade.MEMBER;

        if (channel.getJoinType() == JoinType.REQUEST) {
            grade = ChannelGrade.WAITING;
        }

        channelUserRepository.save(new ChannelUser(channel, user, grade));

        return channel.getId();
    }

    @Transactional
    public Long leaveChannel(Long channelId, String userEmail) {
        Channel channel = validateChannel(channelId);

        User user = userService.getUser(userEmail);

        ChannelUser channelUser = getChannelUser(channel, user);

        if (channelUser.getChannelGrade() == ChannelGrade.ADMIN) {
            throw new IllegalArgumentException("소유자는 채널을 탈퇴할 수 없습니다. 권한을 위임하거나 채널을 삭제하세요.");
        }

        channelUserRepository.delete(channelUser);

        return channel.getId();
    }

    @Transactional
    public Long kickMember(Long channelId, Long targetUserId, String userEmail) {
        Channel channel = validateChannel(channelId);

        validateChannelAdmin(userEmail, channel);

        User targetUser = userService.getUser(targetUserId);
        ChannelUser targetChannelUser = getChannelUser(channel, targetUser);

        if (targetChannelUser.getChannelGrade() == ChannelGrade.ADMIN) {
            throw new IllegalArgumentException("소유자를 채널에서 강퇴할 수 없습니다.");
        }

        channelUserRepository.delete(targetChannelUser);

        return targetUser.getId();
    }
    
    @Transactional
    public Long updateMemberGrade(Long channelId, Long targetUserId, MemberGradeUpdateRequest request, String userEmail) {
        Channel channel = validateChannel(channelId);

        validateChannelAdmin(userEmail, channel);

        User targetUser = userService.getUser(targetUserId);
        ChannelUser targetChannelUser = getChannelUser(channel, targetUser);

        if (targetChannelUser.getChannelGrade() == request.getChannelGrade()) {
            throw new IllegalArgumentException("동일한 권한으로는 변경할 수 없습니다.");
        }

        if (targetChannelUser.getChannelGrade() == ChannelGrade.ADMIN && channelUserRepository.countByChannelIdAndChannelGrade(channelId, ChannelGrade.ADMIN) == 1) {
            throw new IllegalStateException("최소 1명의 소유자가 있어야 합니다.");
        }

        targetChannelUser.updateGrade(request.getChannelGrade());

        return targetUserId;
    }

    public Page<ChannelResponse> getChannels(Pageable pageable) {
        return channelRepository.findAll(pageable).map(ChannelResponse::from);
    }

    public void validateChannelAdmin(String userEmail, Channel channel) {
        User user = userService.getUser(userEmail);
        ChannelUser channelUser = getChannelUser(channel, user);

        if (channelUser.getChannelGrade() != ChannelGrade.ADMIN) {
            throw new AccessDeniedException("소유자 권한이 필요합니다.");
        }
    }

    public Channel validateChannel(Long channelId) {
        Channel channel = getChannel(channelId);

        if (channel.getStatus() == ChannelStatus.INACTIVE) {
            throw new IllegalArgumentException("삭제된 채널입니다.");
        }
        return channel;
    }

    public Page<ChannelResponse> getChannelsOrderByMemberCount(Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        return channelRepository.findAllOrderByMemberCountDesc(pageRequest).map(ChannelResponse::from);
    }

    public Page<ChannelMemberResponse> getMembers(Long channelId, Pageable pageable) {
        return channelUserRepository.findAllByChannelId(channelId, pageable).map(ChannelMemberResponse::from);
    }

    public ChannelUser getChannelUser(Channel channel, User user) {
        return channelUserRepository.findByChannelIdAndUserId(channel.getId(), user.getId())
                .orElseThrow(() -> new IllegalArgumentException("채널에 가입되지 않은 사용자입니다."));
    }

    public Channel getChannel(Long channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("채널 정보를 찾을 수 없습니다."));
    }

}


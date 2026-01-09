package com.popogonry.notid.channel;

import com.popogonry.notid.channel.dto.ChannelCreateRequest;
import com.popogonry.notid.channel.dto.ChannelResponse;
import com.popogonry.notid.channel.dto.ChannelSearchRequest;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

        User user = userRepository.findByEmail(tokenEmail).orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));

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
        userRepository.findByEmail(tokenEmail).orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));

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


}


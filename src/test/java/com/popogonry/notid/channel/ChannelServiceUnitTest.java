package com.popogonry.notid.channel;

import com.popogonry.notid.channel.dto.ChannelCreateRequest;
import com.popogonry.notid.channeluser.ChannelUser;
import com.popogonry.notid.channeluser.ChannelUserRepository;
import com.popogonry.notid.organization.Organization;
import com.popogonry.notid.organization.OrganizationRepository;
import com.popogonry.notid.organizationchannel.OrganizationChannelRepository;
import com.popogonry.notid.user.Gender;
import com.popogonry.notid.user.User;
import com.popogonry.notid.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelServiceUnitTest {

    @InjectMocks
    private ChannelService channelService;

    @Mock
    ChannelRepository channelRepository;

    @Mock
    ChannelUserRepository channelUserRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    OrganizationRepository organizationRepository;

    @Mock
    OrganizationChannelRepository organizationChannelRepository;

    @Mock
    EntityManager em;

    User user;
    Long userId;
    String userEmail;

    Channel channel;
    Long channelId;
    String channelName;

    @BeforeEach
    void setUp() {
        userId = 1L;
        userEmail = "test@gmail.com";

        user = User.builder()
                .email(userEmail)
                .password("encodedPassword")
                .name("name")
                .gender(Gender.ETC)
                .phone("010-0000-0000")
                .build();

        ReflectionTestUtils.setField(user, "id", userId);

        channelId = 100L;
        channelName = "channelName";

        channel = Channel.builder()
                .name(channelName)
                .description("description")
                .joinType(JoinType.FREE)
                .build();

        ReflectionTestUtils.setField(channel, "id", channelId);
    }

    @Test
    @DisplayName("채널 생성 성공 - 조직 없이 생성")
    public void createChannel_success_no_orgs() throws Exception {
        //given
        ChannelCreateRequest request = new ChannelCreateRequest(channelName, "description", JoinType.FREE, new ArrayList<>());

        given(userRepository.findByEmail(userEmail)).willReturn(Optional.of(user));
        given(channelRepository.save(any(Channel.class))).willReturn(channel);

        //when
        Long savedChannelId = channelService.createChannel(request, userEmail);

        //then
        assertThat(savedChannelId).isEqualTo(channelId);

        verify(channelUserRepository, times(1)).save(any(ChannelUser.class));

        verify(organizationRepository, never()).findAllById(any());
        verify(organizationChannelRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("채널 생성 성공 - 조직 포함 생성")
    public void createChannel_success_with_orgs() throws Exception {
        //given
        List<Long> orgIds = List.of(10L, 20L);
        ChannelCreateRequest request = new ChannelCreateRequest(channelName, "description", JoinType.FREE, orgIds);

        given(userRepository.findByEmail(userEmail)).willReturn(Optional.of(user));
        given(channelRepository.save(any(Channel.class))).willReturn(channel);

        Organization org1 = Organization.builder().name("org1").build();
        Organization org2 = Organization.builder().name("org2").build();
        given(organizationRepository.findAllById(any(Set.class))).willReturn(List.of(org1, org2));

        //when
        Long savedChannelId = channelService.createChannel(request, userEmail);

        //then
        assertThat(savedChannelId).isEqualTo(channelId);

        verify(organizationChannelRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("채널 생성 실패 - 존재하지 않는 조직 ID")
    public void createChannel_fail_invalid_org_id() throws Exception {
        //given
        List<Long> orgIds = List.of(10L, 999L);
        ChannelCreateRequest request = new ChannelCreateRequest(channelName, "description", JoinType.FREE, orgIds);

        given(userRepository.findByEmail(userEmail)).willReturn(Optional.of(user));
        given(channelRepository.save(any(Channel.class))).willReturn(channel);

        Organization org1 = Organization.builder().name("org1").build();
        given(organizationRepository.findAllById(any(Set.class))).willReturn(List.of(org1));

        //when
        //then
        assertThatThrownBy(() -> channelService.createChannel(request, userEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 조직 ID가 포함되어 있습니다.");

        verify(organizationChannelRepository, never()).saveAll(any());
    }



}
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
import com.popogonry.notid.user.Gender;
import com.popogonry.notid.user.User;
import com.popogonry.notid.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
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

    @Test
    @DisplayName("채널 검색 성공 - ID 검색")        
    public void searchChannels_success_byId() throws Exception {
        //given
        ChannelSearchRequest request = new ChannelSearchRequest(SearchType.ID, channelId.toString());

        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));

        addOrgs();

        //when
        Page<ChannelResponse> channelResponses = channelService.searchChannels(request, userEmail, Pageable.unpaged());

        //then
        assertChannelResponse(channelResponses);

        verify(channelRepository).findById(channelId);
    }


    @Test
    @DisplayName("채널 검색 성공 - 이름 검색")        
    public void searchChannels_success_byName() throws Exception {
        //given
        ChannelSearchRequest request = new ChannelSearchRequest(SearchType.NAME, channelName);

        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(channelRepository.findByNameContaining(channelName, Pageable.unpaged())).willReturn(new PageImpl<>(List.of(channel)));

        addOrgs();

        //when
        Page<ChannelResponse> channelResponses = channelService.searchChannels(request, userEmail, Pageable.unpaged());

        //then
        assertChannelResponse(channelResponses);

        verify(channelRepository).findByNameContaining(channelName, Pageable.unpaged());
    }
    
    @Test
    @DisplayName("채널 검색 성공 - 설명 검색")        
    public void searchChannels_success_byDes() throws Exception {
        //given
        ChannelSearchRequest request = new ChannelSearchRequest(SearchType.DESCRIPTION, channel.getDescription());

        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(channelRepository.findByDescriptionContaining(channel.getDescription(), Pageable.unpaged())).willReturn(new PageImpl<>(List.of(channel)));

        addOrgs();

        //when
        Page<ChannelResponse> channelResponses = channelService.searchChannels(request, userEmail, Pageable.unpaged());

        //then
        assertChannelResponse(channelResponses);

        verify(channelRepository).findByDescriptionContaining(channel.getDescription(), Pageable.unpaged());
    }

    @Test
    @DisplayName("채널 검색 성공 - 조직 검색")        
    public void searchChannels_success_byOrg() throws Exception {
        //given
        ChannelSearchRequest request = new ChannelSearchRequest(SearchType.ORGANIZATION, "org1");

        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(channelRepository.findByOrganizationName("org1", Pageable.unpaged())).willReturn(new PageImpl<>(List.of(channel)));

        addOrgs();

        //when
        Page<ChannelResponse> channelResponses = channelService.searchChannels(request, userEmail, Pageable.unpaged());

        //then
        assertChannelResponse(channelResponses);

        verify(channelRepository).findByOrganizationName("org1", Pageable.unpaged());
    }
    
    @Test
    @DisplayName("채널 검색 실패 - 존재하지 않는 유저")
    public void searchChannels_fail_user_not_found() throws Exception {
        //given
        ChannelSearchRequest request = new ChannelSearchRequest(SearchType.ID, channelId.toString());

        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> channelService.searchChannels(request, "wrong" + userEmail, Pageable.unpaged()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유저 정보를 찾을 수 없습니다.");
    }
    
    @Test
    @DisplayName("채널 검색 성공 - ID 검색인데 숫자가 아닌 값을 입력 (빈 결과 반환)")        
    public void searchChannels_success_id_invalid_format() throws Exception {
        //given
        ChannelSearchRequest request = new ChannelSearchRequest(SearchType.ID, "notLong");

        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));

        //when
        Page<ChannelResponse> channelResponses = channelService.searchChannels(request, userEmail, Pageable.unpaged());

        //then
        assertThat(channelResponses).isEmpty();

        verify(channelRepository, never()).findById(channelId);
    }
    
    @Test
    @DisplayName("채널 검색 성공 - 존재하지 않는 ID 검색 (빈 결과 반환)")
    public void searchChannels_success_id_not_found() throws Exception {
        //given
        ChannelSearchRequest request = new ChannelSearchRequest(SearchType.ID, Long.toString(channelId + 1L));

        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));

        //when
        Page<ChannelResponse> channelResponses = channelService.searchChannels(request, userEmail, Pageable.unpaged());

        //then
        assertThat(channelResponses).isEmpty();
    }

    @Test
    @DisplayName("채널 검색 성공 - 검색 결과 없음")
    public void searchChannels_success_no_result() throws Exception {
        //given
        ChannelSearchRequest request = new ChannelSearchRequest(SearchType.NAME, "no result");

        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(channelRepository.findByNameContaining("no result", Pageable.unpaged())).willReturn(Page.empty());

        //when
        Page<ChannelResponse> channelResponses = channelService.searchChannels(request, userEmail, Pageable.unpaged());

        //then
        assertThat(channelResponses).isEmpty();
    }

    @Test
    @DisplayName("채널 수정 성공")
    public void updateChannel_success() throws Exception {
        //given
        ChannelUpdateRequest request = new ChannelUpdateRequest("newDes", JoinType.REQUEST, List.of(2L, 3L));

        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(channelUserRepository.findByChannelIdAndUserId(channelId, userId)).willReturn(Optional.of(new ChannelUser(channel, user, ChannelGrade.ADMIN)));

        addOrgs();

        Organization org3 = Organization.builder().name("org3").build();
        ReflectionTestUtils.setField(org3, "id", 3L);

        given(organizationRepository.findAllById(List.of(3L))).willReturn(List.of(org3));

        //when
        channelService.updateChannel(channelId, request, userEmail);

        //then
        assertThat(channel.getDescription()).isEqualTo("newDes");
        assertThat(channel.getJoinType()).isEqualTo(JoinType.REQUEST);
        assertThat(channel.getOrganizationChannels()).hasSize(2)
                .extracting("organization.name")
                .containsExactlyInAnyOrder("org2", "org3");

        verify(organizationRepository).findAllById(any());
    }

    @Test
    @DisplayName("채널 수정 실패 - 존재하지 않는 유저")
    public void updateChannel_fail_channel_not_found() throws Exception {
        //given
        ChannelUpdateRequest request = new ChannelUpdateRequest("newDes", JoinType.REQUEST, List.of(2L, 3L));

        //when
        //then
        assertThatThrownBy(() -> channelService.updateChannel(channelId, request, userEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("채널 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("채널 수정 실패 - 존재하지 않는 유저")
    public void updateChannel_fail_user_not_found() throws Exception {
        //given
        ChannelUpdateRequest request = new ChannelUpdateRequest("newDes", JoinType.REQUEST, List.of(2L, 3L));

        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));

        //when
        //then
        assertThatThrownBy(() -> channelService.updateChannel(channelId, request, userEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유저 정보를 찾을 수 없습니다.");
    }
    
    @Test
    @DisplayName("채널 수정 실패 - 유저 채널 미가입")
    public void updateChannel_fail_user_not_in_channel() throws Exception {
        //given
        ChannelUpdateRequest request = new ChannelUpdateRequest("newDes", JoinType.REQUEST, List.of(2L, 3L));

        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));

        //when
        //then
        assertThatThrownBy(() -> channelService.updateChannel(channelId, request, userEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("채널에 가입되지 않은 유저입니다.");
    }

    @Test
    @DisplayName("채널 수정 실패 - 유저 권한 없음")
    public void updateChannel_fail_user_access_denied() throws Exception {
        //given
        ChannelUpdateRequest request = new ChannelUpdateRequest("newDes", JoinType.REQUEST, List.of(2L, 3L));

        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(channelUserRepository.findByChannelIdAndUserId(channelId, userId)).willReturn(Optional.of(new ChannelUser(channel, user, ChannelGrade.MANAGER)));

        //when
        //then
        assertThatThrownBy(() -> channelService.updateChannel(channelId, request, userEmail))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("관리자 권한이 필요합니다.");
    }
    
    @Test
    @DisplayName("채널 수정 실패 - 존재하는 않는 조직")
    public void updateChannel_fail_org_id_not_found() throws Exception {
        //given
        ChannelUpdateRequest request = new ChannelUpdateRequest("newDes", JoinType.REQUEST, List.of(5L, 6L));

        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(channelUserRepository.findByChannelIdAndUserId(channelId, userId)).willReturn(Optional.of(new ChannelUser(channel, user, ChannelGrade.ADMIN)));

        addOrgs();

        Organization org3 = Organization.builder().name("org3").build();
        ReflectionTestUtils.setField(org3, "id", 3L);

        given(organizationRepository.findAllById(any())).willReturn(List.of(org3));

        //when
        //then
        assertThatThrownBy(() -> channelService.updateChannel(channelId, request, userEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 조직 ID가 포함되어 있습니다.");
    }
    
    private void addOrgs() {
        Organization org1 = Organization.builder().name("org1").build();
        Organization org2 = Organization.builder().name("org2").build();

        ReflectionTestUtils.setField(org1, "id", 1L);
        ReflectionTestUtils.setField(org2, "id", 2L);

        channel.addOrganizationChannel(new OrganizationChannel(channel, org1));
        channel.addOrganizationChannel(new OrganizationChannel(channel, org2));
    }
    
    private void assertChannelResponse(Page<ChannelResponse> channelResponses) {
        assertThat(channelResponses.getTotalElements()).isEqualTo(1);

        ChannelResponse response = channelResponses.getContent().getFirst();

        assertThat(response.getId()).isEqualTo(channelId);
        assertThat(response.getName()).isEqualTo(channelName);

        assertThat(response.getOrganizationChannels()).hasSize(2)
                .extracting("name")
                .containsExactlyInAnyOrder("org1", "org2");
    }


}
package com.popogonry.notid.notice;

import com.popogonry.notid.channel.Channel;
import com.popogonry.notid.channel.ChannelRepository;
import com.popogonry.notid.channel.ChannelService;
import com.popogonry.notid.channel.JoinType;
import com.popogonry.notid.channeluser.ChannelGrade;
import com.popogonry.notid.channeluser.ChannelUser;
import com.popogonry.notid.notice.dto.NoticeCreateRequest;
import com.popogonry.notid.user.Gender;
import com.popogonry.notid.user.User;
import com.popogonry.notid.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class NoticeServiceUnitTest {

    @InjectMocks
    private NoticeService noticeService;

    @Mock
    NoticeRepository noticeRepository;

    @Mock
    UserService userService;

    @Mock
    ChannelService channelService;

    @Mock
    ChannelRepository channelRepository;


    User user;
    Long userId;
    String userEmail;

    Channel channel;
    Long channelId;
    String channelName;

    Notice notice;
    Long noticeId;

    boolean replyRequired = true;

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

        noticeId = 1000L;
        notice = Notice.builder()
                .title("title")
                .content("content")
                .deadline(LocalDateTime.now().plusDays(7))
                .replyRequired(true)
                .user(user)
                .channel(channel)
                .build();

        ReflectionTestUtils.setField(notice, "id", noticeId);
    }

    @Test
    @DisplayName("공지 생성 성공")
    public void createNotice_success() throws Exception {
        //given
        NoticeCreateRequest request = new NoticeCreateRequest("title", "content", LocalDateTime.now().plusDays(7), true);

        given(userService.getUser(userEmail)).willReturn(user);
        given(channelService.getChannel(channelId)).willReturn(channel);

        given(noticeRepository.save(any(Notice.class))).willReturn(notice);


        //when
        Long savedNoticeId = noticeService.createNotice(request, userEmail, channelId);

        //then

        assertThat(savedNoticeId).isEqualTo(noticeId);

        verify(noticeRepository, times(1)).save(any(Notice.class));
    }
}
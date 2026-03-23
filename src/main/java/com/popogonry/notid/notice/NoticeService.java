package com.popogonry.notid.notice;

import com.popogonry.notid.channel.Channel;
import com.popogonry.notid.channel.ChannelRepository;
import com.popogonry.notid.channel.ChannelService;
import com.popogonry.notid.channeluser.ChannelUser;
import com.popogonry.notid.notice.dto.NoticeCreateRequest;
import com.popogonry.notid.user.User;
import com.popogonry.notid.user.UserRepository;
import com.popogonry.notid.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserService userService;
    private final ChannelService channelService;


    @Transactional
    public Long createNotice(NoticeCreateRequest request, String userEmail, Long channelId) {
        // 1. 존재하는 유저인가?
        User user = userService.getUser(userEmail);

        // 2. 존재하는 채널인가?
        Channel channel = channelService.getChannel(channelId);

        // 3. 채널에 유저가 속해 있는가?
        // 4. 유저가 채널에 공지를 쓸 수 있는 권한이 있는가?
        channelService.validateChannelManager(userEmail, channel);


        Notice notice = noticeRepository.save(new Notice(
                request.getTitle(),
                request.getContent(),
                request.getDeadline(),
                request.isReplyRequired(),
                user,
                channel
        ));

        return notice.getId();
    }
}

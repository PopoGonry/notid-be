package com.popogonry.notid.notice.dto;

import com.popogonry.notid.channel.Channel;
import com.popogonry.notid.channel.dto.ChannelResponse;
import com.popogonry.notid.notice.Notice;
import com.popogonry.notid.user.User;
import com.popogonry.notid.user.dto.UserResponse;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NoticeResponse {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime deadline;
    private boolean replyRequired = true;
    private UserResponse user;
    private ChannelResponse channel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NoticeResponse from(Notice notice) {
        return new NoticeResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getDeadline(),
                notice.isReplyRequired(),
                UserResponse.from(notice.getUser()),
                ChannelResponse.from(notice.getChannel()),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
                );
    }
}

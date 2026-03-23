package com.popogonry.notid.notice.dto;

import com.popogonry.notid.channel.Channel;
import com.popogonry.notid.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NoticeCreateRequest {

    @NotBlank(message = "공지 제목은 필수입니다.")
    private String title;

    private String content;

    private LocalDateTime deadline;

    private boolean replyRequired = true;
}

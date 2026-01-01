package com.popogonry.notid.notice;

import com.popogonry.notid.channel.Channel;
import com.popogonry.notid.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "notices")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notices_id")
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime deadline;

    @Column(nullable = false)
    private boolean replyRequired = true;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "channels_id", nullable = false)
    private Channel channel;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Notice(String title, String content, LocalDateTime deadline, Boolean replyRequired, User user, Channel channel) {
        this.title = title;
        this.content = content;
        this.deadline = deadline;
        // null이 들어오면 기본값(true) 유지, 아니면 값 설정
        this.replyRequired = (replyRequired != null) ? replyRequired : true;
        this.user = user;
        this.channel = channel;
    }
}

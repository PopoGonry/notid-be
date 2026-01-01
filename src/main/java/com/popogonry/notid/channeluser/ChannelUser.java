package com.popogonry.notid.channeluser;

import com.popogonry.notid.channel.Channel;
import com.popogonry.notid.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "channel_users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_channel_user",
                        columnNames = {"channels_id", "users_id"}
                )
        }
)
public class ChannelUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "channel_users_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "channels_id")
    private Channel channel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "users_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelGrade channelGrade = ChannelGrade.MEMBER;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public ChannelUser(Channel channel, User user, ChannelGrade channelGrade) {
        this.channel = channel;
        this.user = user;
        this.channelGrade = (channelGrade == null) ? ChannelGrade.MEMBER : channelGrade;
    }

    public void updateGrade(ChannelGrade newGrade) {
        this.channelGrade = newGrade;
    }
}

package com.popogonry.notid.channel;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "channels")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "channels_id")
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JoinType joinType = JoinType.FREE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelStatus status = ChannelStatus.ACTIVE;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @Builder
    public Channel(String name, String description, JoinType joinType) {
        this.name = name;
        this.description = description;
        this.joinType = (joinType == null) ? JoinType.FREE : joinType;
    }

    public void inactive() {
        this.status = ChannelStatus.INACTIVE;
    }
}

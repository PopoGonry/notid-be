package com.popogonry.notid.channel;


import com.popogonry.notid.organizationchannel.OrganizationChannel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrganizationChannel> organizationChannels = new ArrayList<>();

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

    public void addOrganizationChannel(OrganizationChannel organizationChannel) {
        organizationChannels.add(organizationChannel);
        organizationChannel.setChannel(this);
    }

    public void removeOrganizationChannel(OrganizationChannel organizationChannel) {
        organizationChannels.remove(organizationChannel);
        organizationChannel.setChannel(null);
    }

    public void updateChannel(String description, JoinType joinType) {
        this.description = description;
        this.joinType = joinType;
    }

    public void inactive() {
        this.status = ChannelStatus.INACTIVE;
        this.organizationChannels.clear();
    }
}

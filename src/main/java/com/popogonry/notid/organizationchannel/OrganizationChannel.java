package com.popogonry.notid.organizationchannel;

import com.popogonry.notid.channel.Channel;
import com.popogonry.notid.organization.Organization;
import jakarta.persistence.*;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "organization_channels",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_organization_channel",
                        columnNames = {"organizations_id", "channels_id"}
                )
        }
)
public class OrganizationChannel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "organization_channels_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "channels_id", nullable = false)
    private Channel channel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizations_id", nullable = false)
    private Organization organization;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public OrganizationChannel(Channel channel, Organization organization) {
        this.channel = channel;
        this.organization = organization;
    }
}

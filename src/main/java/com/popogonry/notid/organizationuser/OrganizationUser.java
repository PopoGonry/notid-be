package com.popogonry.notid.organizationuser;

import com.popogonry.notid.organization.Organization;
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
        name = "organization_users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_organization_user",
                        columnNames = {"organizations_id", "users_id"}
                )
        }
)
public class OrganizationUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "organization_users_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizations_id", nullable = false)
    private Organization organization;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public OrganizationUser(User user, Organization organization) {
        this.user = user;
        this.organization = organization;
    }
}

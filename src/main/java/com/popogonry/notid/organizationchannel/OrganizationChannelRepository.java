package com.popogonry.notid.organizationchannel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationChannelRepository extends JpaRepository<OrganizationChannel, Long> {
}

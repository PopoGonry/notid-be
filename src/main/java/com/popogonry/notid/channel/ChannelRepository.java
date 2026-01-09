package com.popogonry.notid.channel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {

    Page<Channel> findByNameContaining(String name, Pageable pageable);

    Page<Channel> findByDescriptionContaining(String description, Pageable pageable);

    @Query(value = "select distinct c from Channel c " +
            "join c.organizationChannels oc " +
            "join oc.organization o " +
            "where o.name like %:keyword%",

            // ★ 필수: 카운트 쿼리도 직접 명시
            countQuery = "select count(distinct c) from Channel c " +
                    "join c.organizationChannels oc " +
                    "join oc.organization o " +
                    "where o.name like %:keyword%")
    Page<Channel> findByOrganizationName(@Param("keyword") String keyword, Pageable pageable);
}

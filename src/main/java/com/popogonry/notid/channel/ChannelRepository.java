package com.popogonry.notid.channel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


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

    @Query(value = "SELECT c FROM Channel c " +
            "LEFT JOIN ChannelUser cu ON cu.channel = c " +
            "GROUP BY c " +
            "ORDER BY COUNT(cu) DESC",
            countQuery = "SELECT COUNT(c) FROM Channel c"
    )
    Page<Channel> findAllOrderByMemberCountDesc(Pageable pageable);
}

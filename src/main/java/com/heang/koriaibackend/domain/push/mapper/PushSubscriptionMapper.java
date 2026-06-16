package com.heang.koriaibackend.domain.push.mapper;

import com.heang.koriaibackend.domain.push.model.PushSubscription;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PushSubscriptionMapper {

    /** Insert a subscription, or re-point an existing endpoint to this user/keys. */
    int upsert(PushSubscription subscription);

    List<PushSubscription> findByUserId(@Param("userId") Long userId);

    /** Remove a dead/unsubscribed endpoint (called on 404/410 from the push service). */
    int deleteByEndpoint(@Param("endpoint") String endpoint);
}

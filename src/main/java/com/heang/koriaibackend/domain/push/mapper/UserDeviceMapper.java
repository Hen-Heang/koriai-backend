package com.heang.koriaibackend.domain.push.mapper;

import com.heang.koriaibackend.domain.push.model.UserDevice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserDeviceMapper {

    /** Register a device token, or re-point an existing token to this user. */
    int upsert(UserDevice device);

    List<UserDevice> findByUserId(@Param("userId") Long userId);

    /** Remove a stale/unregistered token (called when FCM reports it invalid). */
    int deleteByToken(@Param("fcmToken") String fcmToken);
}

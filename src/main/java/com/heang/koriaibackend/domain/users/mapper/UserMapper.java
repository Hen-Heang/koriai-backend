package com.heang.koriaibackend.domain.users.mapper;

import com.heang.koriaibackend.domain.users.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface UserMapper {

    int insert(User user);

    Optional<User> findById(@Param("id") Long id);

    Optional<User> findByEmail(@Param("email") String email);

    int updateProfile(@Param("id") Long id,
                      @Param("displayName") String displayName,
                      @Param("koreanLevel") String koreanLevel);

    int updatePreferredModel(@Param("id") Long id, @Param("preferredModel") String preferredModel);

    int deleteById(@Param("id") Long id);
}

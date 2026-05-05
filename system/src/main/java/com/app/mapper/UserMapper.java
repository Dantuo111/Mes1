package com.app.mapper;

import com.app.entity.User;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface UserMapper {

    User selectById(Long id);

    User selectByUsername(String username);

    List<User> selectList(@Param("keyword") String keyword, @Param("role") Integer role, @Param("status") Integer status);

    int insert(User user);

    int updateById(User user);

    int deleteById(Long id);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int updatePassword(@Param("id") Long id, @Param("password") String password);

    int countAll();
}

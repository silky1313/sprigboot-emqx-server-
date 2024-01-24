package com.silky.server.domain.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.silky.server.domain.po.Users;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author silky
 * @since 2024-01-23
 */
public interface UsersMapper extends BaseMapper<Users> {

    default Users findUserByUsernameAndPassword(String username, String password) {
        return selectOne(Wrappers.<Users>lambdaQuery().eq(Users::getUsername, username).eq(Users::getPassword, password));
    }
}

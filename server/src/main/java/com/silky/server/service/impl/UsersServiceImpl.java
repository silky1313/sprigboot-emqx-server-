package com.silky.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.silky.server.domain.po.Users;
import com.silky.server.domain.mapper.UsersMapper;
import com.silky.server.service.IUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.silky.server.utils.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author silky
 * @since 2024-01-23
 */
@Service
@Slf4j
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {

    @Autowired
    UsersMapper usersMapper;

    @Override
    public Response login(Users users) {
        log.info("{}", users.getUsername());
        Users user = usersMapper.findUserByUsernameAndPassword(users.getUsername(), users.getPassword());
        if(user != null) {
            return Response.success(user);
        } else {
            return Response.fail("500", "密码错误");
        }
    }
}

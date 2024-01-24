package com.silky.server.service;

import com.silky.server.domain.po.Users;
import com.baomidou.mybatisplus.extension.service.IService;
import com.silky.server.utils.Response;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author silky
 * @since 2024-01-23
 */
public interface IUsersService extends IService<Users> {

    Response login(Users users);
}

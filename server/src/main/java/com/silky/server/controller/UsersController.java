package com.silky.server.controller;


import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.silky.server.domain.po.Users;
import com.silky.server.service.IUsersService;
import com.silky.server.utils.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器,其实登录模块可以直接不写的，实际比赛中没什么用。但是为了符合前端的需求，还是写上了。
 * </p>
 *
 * @author silky
 * @since 2024-01-23
 */
@RestController
@Api(tags = "用户模块")
public class UsersController {

    @Autowired
    IUsersService usersService;

    @PostMapping("/login")
    @ApiOperation(value = "登录接口")
    @ApiOperationSupport(ignoreParameters = {"authname","userid"})
    public Response login(@RequestBody Users users) {
        return usersService.login(users);
    }

    @GetMapping("/userinfo")
    @ApiOperation(value = "获取用户信息")
    public Response getUserInfo() {
        Users users = usersService.getById(1);
        return Response.success(users);
    }
}

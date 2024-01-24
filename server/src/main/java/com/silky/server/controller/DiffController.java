package com.silky.server.controller;


import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.silky.server.domain.po.Diff;
import com.silky.server.service.IDiffService;
import com.silky.server.utils.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author silky
 * @since 2024-01-23
 */
@RestController
@RequestMapping
@Api(tags = "硬件数据模块")
public class DiffController {

    @Autowired
    IDiffService diffService;

    @GetMapping("/mqtt")
    @ApiOperation(value = "获取mqtt信息")
    public Response mqtt() {
        List<Diff> diffs = diffService.list();
        return Response.success(diffs);
    }

    @PostMapping("/addmqtt")
    @ApiOperation(value = "添加diff")
    @ApiOperationSupport(ignoreParameters = {"dataid","time"})
    public Response addMqtt(@RequestBody  Diff diff) {
        boolean save = diffService.saveOne(diff);
        if(!save) {
            return Response.fail("500", "增加数据失败");
        }
        return Response.success(save,"增加数据成功");
    }

    @PostMapping("/send")
    @ApiOperation(value = "发送mqtt信息")
    public Response send(@RequestBody String message) {
        boolean send1 = diffService.send(message);
        if(!send1) {
            return Response.fail("500", "发送失败");
        }
        return Response.success(send1,"发送成功");
    }
}

package com.silky.server.service;

import com.silky.server.domain.po.Diff;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author silky
 * @since 2024-01-23
 */
public interface IDiffService extends IService<Diff> {

    boolean saveOne(Diff diff);

    boolean send(String message);
}

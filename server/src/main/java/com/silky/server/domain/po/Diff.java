package com.silky.server.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author silky
 * @since 2024-01-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("diff")
@Builder
public class Diff implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "dataid", type = IdType.AUTO)
    private Integer dataid;

    private String imei;

    private String temp;

    private String humi;

    private LocalDateTime time;


}

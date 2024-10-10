package com.huayang.product.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户和岗位关联 sys_user_post
 *
 * @author huayang
 */
@Data
@TableName("sys_user_post")
public class SysUserPost {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 岗位ID
     */
    private Long postId;
}

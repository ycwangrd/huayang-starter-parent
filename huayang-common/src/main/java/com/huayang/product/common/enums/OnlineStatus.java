package com.huayang.product.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户会话
 *
 * @author huayang
 */
@AllArgsConstructor
@Getter
public enum OnlineStatus {
    /**
     * 用户状态
     */
    on_line("在线"), off_line("离线");

    private final String info;
}

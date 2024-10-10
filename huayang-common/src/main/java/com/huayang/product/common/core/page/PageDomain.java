package com.huayang.product.common.core.page;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.dromara.hutool.core.text.StrUtil;

/**
 * 分页数据
 *
 * @author huayang
 */
@Data
public class PageDomain {
    /**
     * 当前记录起始索引
     */
    private Integer pageNum;

    /**
     * 每页显示记录数
     */
    private Integer pageSize;

    /**
     * 排序列
     */
    private String orderByColumn;

    /**
     * 排序的方向desc或者asc
     */
    private String isAsc = "asc";

    /**
     * 分页参数合理化
     */
    private Boolean reasonable = true;

    public String getOrderBy() {
        if (StringUtils.isEmpty(orderByColumn)) {
            return "";
        }
        return StrUtil.toUnderlineCase(orderByColumn) + " " + isAsc;
    }

    public Boolean getReasonable() {
        if (reasonable != null && reasonable) {
            return Boolean.TRUE;
        }
        return reasonable;
    }
}

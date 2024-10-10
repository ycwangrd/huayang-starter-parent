package com.huayang.product.common.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * CxSelect树结构实体类
 *
 * @author huayang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CxSelect implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 数据值字段名称
     */
    private String v;

    /**
     * 数据标题字段名称
     */
    private String n;

    /**
     * 子集数据字段名称
     */
    private List<CxSelect> s;

    public CxSelect(String v, String n) {
        this.v = v;
        this.n = n;
    }
}

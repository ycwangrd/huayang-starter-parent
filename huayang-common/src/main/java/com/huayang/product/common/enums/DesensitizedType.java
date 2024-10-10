package com.huayang.product.common.enums;

import lombok.AllArgsConstructor;
import org.dromara.hutool.core.data.MaskingUtil;

import java.util.function.Function;

/**
 * 脱敏类型
 *
 * @author huayang
 */
@AllArgsConstructor
public enum DesensitizedType {
    /**
     * 姓名，第2位星号替换
     */
    USERNAME(MaskingUtil::chineseName),

    /**
     * 密码，全部字符都用*代替
     */
    PASSWORD(MaskingUtil::password),

    /**
     * 身份证，中间10位星号替换
     */
    ID_CARD(s -> MaskingUtil.idCardNum(s, 4, 4)),

    /**
     * 手机号，中间4位星号替换
     */
    PHONE(MaskingUtil::mobilePhone),

    /**
     * 电子邮箱，仅显示第一个字母和@后面的地址显示，其他星号替换
     */
    EMAIL(MaskingUtil::email),

    /**
     * 银行卡号，保留最后4位，其他星号替换
     */
    BANK_CARD(MaskingUtil::bankCard),

    /**
     * 车牌号码，包含普通车辆、新能源车辆
     */
    CAR_LICENSE(MaskingUtil::carLicense);

    private final Function<String, String> desensitizer;

    public Function<String, String> desensitizer() {
        return desensitizer;
    }
}

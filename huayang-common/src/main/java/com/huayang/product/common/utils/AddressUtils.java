package com.huayang.product.common.utils;

import com.huayang.product.common.config.HmcConfig;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.text.StrUtil;
import org.dromara.hutool.core.util.CharsetUtil;
import org.dromara.hutool.http.HttpUtil;
import org.dromara.hutool.json.JSONObject;
import org.dromara.hutool.json.JSONUtil;

/**
 * 获取地址类
 *
 * @author huayang
 */
@Slf4j
public class AddressUtils {
    // IP地址查询
    public static final String IP_URL = "http://whois.pconline.com.cn/ipJson.jsp";
    // 未知地址
    public static final String UNKNOWN = "XX XX";

    public static String getRealAddressByIP(String ip) {
        // 内网不查询
        if (IpUtils.internalIp(ip)) {
            return "内网IP";
        }
        if (HmcConfig.isAddressEnabled()) {
            try {
                String rspStr = HttpUtil.get(IP_URL + "?ip=" + ip + "&json=true", CharsetUtil.GBK);
                if (StrUtil.isEmpty(rspStr)) {
                    log.error("获取地理位置异常 {}", ip);
                    return UNKNOWN;
                }
                JSONObject obj = JSONUtil.toBean(rspStr, JSONObject.class);
                String region = obj.getStr("pro");
                String city = obj.getStr("city");
                return String.format("%s %s", region, city);
            } catch (Exception e) {
                log.error("获取地理位置异常 {}", e);
            }
        }
        return UNKNOWN;
    }
}

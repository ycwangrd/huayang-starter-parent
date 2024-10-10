package com.huayang.product.common.core.text;

import org.apache.commons.lang3.StringUtils;
import org.dromara.hutool.core.convert.ConvertUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 类型转换器
 *
 * @author huayang
 */
public class HmcConvertUtil extends ConvertUtil {
    /**
     * 转换为boolean<br>
     * String支持的值为：true、false、yes、ok、no，1,0 如果给定的值为空，或者转换失败，返回默认值<br>
     * 转换失败不会报错
     *
     * @param value        被转换的值
     * @param defaultValue 转换错误时的默认值
     * @return 结果
     */
    public static Boolean toBool(Object value, Boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }
        valueStr = valueStr.trim().toLowerCase();
        switch (valueStr) {
            case "true":
            case "yes":
            case "ok":
            case "1":
                return true;
            case "false":
            case "no":
            case "0":
                return false;
            default:
                return defaultValue;
        }
    }

    /**
     * 转换为boolean<br>
     * 如果给定的值为空，或者转换失败，返回默认值<code>null</code><br>
     * 转换失败不会报错
     *
     * @param value 被转换的值
     * @return 结果
     */
    public static Boolean toBool(Object value) {
        return toBool(value, null);
    }

    /**
     * 数字金额大写转换 先写个完整的然后将如零拾替换成零
     *
     * @param n 数字
     * @return 中文大写数字
     */
    public static String digitUppercase(double n) {
        String[] fraction = {"角", "分"};
        String[] digit = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
        String[][] unit = {{"元", "万", "亿"}, {"", "拾", "佰", "仟"}};

        String head = n < 0 ? "负" : "";
        n = Math.abs(n);

        String s = "";
        for (int i = 0; i < fraction.length; i++) {
            // 优化double计算精度丢失问题
            BigDecimal nNum = new BigDecimal(n);
            BigDecimal decimal = new BigDecimal(10);
            BigDecimal scale = nNum.multiply(decimal).setScale(2, RoundingMode.HALF_EVEN);
            double d = scale.doubleValue();
            s += (digit[(int) (Math.floor(d * Math.pow(10, i)) % 10)] + fraction[i]).replaceAll("(零.)+", "");
        }
        if (s.length() < 1) {
            s = "整";
        }
        int integerPart = (int) Math.floor(n);

        for (int i = 0; i < unit[0].length && integerPart > 0; i++) {
            String p = "";
            for (int j = 0; j < unit[1].length && n > 0; j++) {
                p = digit[integerPart % 10] + unit[1][j] + p;
                integerPart = integerPart / 10;
            }
            s = p.replaceAll("(零.)*零$", "").replaceAll("^$", "零") + unit[0][i] + s;
        }
        return head + s.replaceAll("(零.)*零元", "元").replaceFirst("(零.)+", "").replaceAll("(零.)+", "零").replaceAll("^整$", "零元整");
    }
}

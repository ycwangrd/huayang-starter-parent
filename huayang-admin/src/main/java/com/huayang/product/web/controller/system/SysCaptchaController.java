package com.huayang.product.web.controller.system;

import com.huayang.product.common.constant.ShiroConstants;
import com.huayang.product.common.core.controller.BaseController;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.dromara.hutool.core.math.Calculator;
import org.dromara.hutool.swing.captcha.ShearCaptcha;
import org.dromara.hutool.swing.captcha.generator.MathGenerator;
import org.dromara.hutool.swing.captcha.generator.RandomGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 图片验证码（支持算术形式）
 *
 * @author huayang
 */
@Controller
@RequestMapping("/captcha")
public class SysCaptchaController extends BaseController {
    /**
     * 生成数学公式验证码
     */
    private MathGenerator mathGenerator = new MathGenerator(1);
    /**
     * 随机字符验证码生成器
     */
    private RandomGenerator randomGenerator = new RandomGenerator(4);

    /**
     * 验证码生成
     */
    @GetMapping(value = "/captchaImage")
    public ModelAndView getCaptchaImage(HttpServletRequest request, HttpServletResponse response) {
        try (ServletOutputStream out = response.getOutputStream()) {
            HttpSession session = request.getSession();
            response.setDateHeader("Expires", 0);
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
            response.addHeader("Cache-Control", "post-check=0, pre-check=0");
            response.setHeader("Pragma", "no-cache");
            response.setContentType("image/jpeg");

            String type = request.getParameter("type");
            String capStr = "";
            String code = null;
            if (StringUtils.equalsIgnoreCase("math", type)) {
                capStr = mathGenerator.generate();
                code = String.valueOf((int) Calculator.conversion(capStr));
            } else if ("char".equals(type)) {
                capStr = code = randomGenerator.generate();
            }
            // 验证码放入session中
            session.setAttribute(ShiroConstants.KAPTCHA_SESSION_KEY, code);
            // 使用验证码生成图片
            ShearCaptcha captcha = new ShearCaptcha(300, 120, capStr.length(), 2);
            Image image = captcha.createImage(capStr);
            ImageIO.write((BufferedImage) image, "png", out);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
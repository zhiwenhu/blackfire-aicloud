package com.blackfire.aicloud.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * @Author: hanbaoqiang
 * @Date: 2020/4/8 13:41
 */
public class VerifyCodeUtil {

    /**
     * 放到session中的key
     */
    public static final String RANDOMCODEKEY = "RANDOMREDISKEY";
    /**
     * 随机产生数字与字母组合的字符串
     */
    private final String randString = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    /**
     * 图片宽
     */
    private final int width = 95;
    /**
     * 图片高
     */
    private final int height = 25;

    private SecureRandom random;

    {
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获得字体
     */
    private Font getFont() {
        return new Font("Fixedsys", Font.BOLD, 18);
    }

    /**
     * 获得颜色
     */
    private Color getRandColor(int fc, int bc) {
        int colorMaxValue = 255;
        if (fc > colorMaxValue) {
            fc = colorMaxValue;
        }
        if (bc > colorMaxValue) {
            bc = colorMaxValue;
        }
        int r = fc + random.nextInt(bc - fc - 16);
        int g = fc + random.nextInt(bc - fc - 14);
        int b = fc + random.nextInt(bc - fc - 18);
        return new Color(r, g, b);
    }

    /**
     * 生成随机图片
     */
    public void getRandcode(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        // BufferedImage类是具有缓冲区的Image类,Image类是用于描述图像信息的类
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        // 产生Image对象的Graphics对象,改对象可以在图像上进行各种绘制操作
        Graphics g = image.getGraphics();
        //图片大小
        g.fillRect(0, 0, width, height);
        //字体大小
        g.setFont(new Font("Times New Roman", Font.PLAIN, 18));
        //字体颜色
        g.setColor(getRandColor(110, 133));
        // 绘制干扰线
        /*
          干扰线数量
         */
        int lineSize = 40;
        for (int i = 0; i <= lineSize; i++) {
            drowLine(g);
        }
        // 绘制随机字符
        String randomString = "";
        /*
          随机产生字符数量
         */
        int stringNum = 4;
        for (int i = 1; i <= stringNum; i++) {
            randomString = drowString(g, randomString, i);
        }
        //将生成的随机字符串保存到session中
        session.removeAttribute(RANDOMCODEKEY);
        session.setAttribute(RANDOMCODEKEY, randomString);
        //设置失效时间1分钟
        session.setMaxInactiveInterval(60);
        g.dispose();
        try {
            // 将内存中的图片通过流动形式输出到客户端
            ImageIO.write(image, "JPEG", response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getRandcode(HttpServletRequest request) {
        HttpSession session = request.getSession();
        // BufferedImage类是具有缓冲区的Image类,Image类是用于描述图像信息的类
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 产生Image对象的Graphics对象,改对象可以在图像上进行各种绘制操作
        Graphics g = image.getGraphics();
        //图片大小
        g.fillRect(0, 0, width, height);
        //字体大小
        g.setFont(new Font("Times New Roman", Font.PLAIN, 18));
        //字体颜色
        g.setColor(getRandColor(110, 133));
        // 绘制干扰线
        /*
          干扰线数量
         */
        int lineSize = 40;
        for (int i = 0; i <= lineSize; i++) {
            drowLine(g);
        }
        // 绘制随机字符
        String randomString = "";
        /*
          随机产生字符数量
         */
        int stringNum = 4;
        for (int i = 1; i <= stringNum; i++) {
            randomString = drowString(g, randomString, i);
        }
        //将生成的随机字符串保存到session中
        session.removeAttribute(RANDOMCODEKEY);
        session.setAttribute(RANDOMCODEKEY, randomString);
        //设置失效时间1分钟
        session.setMaxInactiveInterval(60);
        g.dispose();
        try {
            // 将内存中的图片通过流动形式输出到客户端
            ImageIO.write(image, "JPEG", baos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] bytes = baos.toByteArray();

        return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes);

    }

    /**
     * 绘制字符串
     */
    private String drowString(Graphics g, String randomString, int i) {
        g.setFont(getFont());
        g.setColor(new Color(random.nextInt(101), random.nextInt(111), random
                .nextInt(121)));
        String rand = String.valueOf(getRandomString(random.nextInt(randString
                .length())));
        randomString += rand;
        g.translate(random.nextInt(3), random.nextInt(3));
        g.drawString(rand, 13 * i, 16);
        return randomString;
    }

    /**
     * 绘制干扰线
     */
    private void drowLine(Graphics g) {
        int x = random.nextInt(width);
        int y = random.nextInt(height);
        int xl = random.nextInt(13);
        int yl = random.nextInt(15);
        g.drawLine(x, y, x + xl, y + yl);
    }

    /**
     * 获取随机的字符
     */
    public String getRandomString(int num) {
        return String.valueOf(randString.charAt(num));
    }

    /**
     * 获取固定长度的随机字符
     */
    public String createRandomString(int stringLength) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stringLength; i++) {
            String rString = getRandomString(random.nextInt(randString.length()));
            sb.append(rString);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        VerifyCodeUtil verifyCodeUtil = new VerifyCodeUtil();
        System.out.println(verifyCodeUtil.createRandomString(4));
    }
}

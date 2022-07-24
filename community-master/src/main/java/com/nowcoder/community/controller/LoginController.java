package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.nowcoder.community.util.CommunityConstant.*;

@Controller
public class LoginController {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;
    @Autowired
    private Producer producer;
    @Value("${server.servlet.context-path}")
    String contextPath;

    private static final Logger logger= LoggerFactory.getLogger(LoginController.class);

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    //这里跳转的是templates下的内容,这里用return来获取。input的name属性和bean的变量名对应才能成功取到值
    //  在表单提交的时候，会根据同名对user进行赋值
    //springmvc user封装到model里，前端页面直接可以用user的属性。confirmpassword从request里可以取到，用param即可
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        // map==null 说明 用户输入参数无问题
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功,我们已经向您的邮箱发送了一封激活邮件,请尽快激活!");
            model.addAttribute("target", "/index");//  返回页面显示的链接：跳转页面
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("confirmPasswordMsg", map.get("confirmPasswordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    @GetMapping("/activation/{userId}/{code}")
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code){
        int result = userService.activation(userId,code);
        if (result==ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功，您的账号已经可以正常使用了");
            model.addAttribute("target","/login");
        }else if (result==ACTIVATION_REPEAT){
            model.addAttribute("msg","无效操作，该账号已激活");
            model.addAttribute("target","/index");
        }else {
            model.addAttribute("msg","激活失败，您的提供的激活码错误");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }


    // 获取验证码的方法
    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response, HttpSession session) {
        // 生成验证码
        String text = producer.createText();
        // 生成验证码图片
        BufferedImage image = producer.createImage(text);
        // 将验证码存到session
//        session.setAttribute("kaptcha", text);

        // 将随机字符串存入cookie中，供登录时取出相应的值在redis查询
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);  //60秒过期
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        // 将验证码存入Redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        //60秒过期  Redis验证码
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);

        // 将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            // 输出(图片文件对象，格式，使用的流)
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    @PostMapping("/login")
    //从session中把验证码取出来，登录成功后将cookie发放给数据库保存
    public String login(String username, String password, String code, boolean rememberme,
                        Model model/*HttpSession session,*/,@CookieValue("kaptchaOwner") String kaptchaOwner,
                        HttpServletResponse response)
    {
        // 检查验证码
        //String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }

       if(StringUtils.isBlank(code) || StringUtils.isBlank(kaptcha) || !kaptcha.equalsIgnoreCase(code))
       {
           model.addAttribute("codeMsg","验证码不正确！");
           return "/site/login";

       }

       // 检查账号密码
       int expiredSeconds=rememberme ? REMEMBER_EXPIRED_SECONDS:DEFAULT_EXPIRED_SECONDS;
       // 调用service
       Map<String,Object> map=userService.login(username,password,expiredSeconds);

       if(map.containsKey("ticket"))
       {
           Cookie cookie=new Cookie("ticket",map.get("ticket").toString());
           cookie.setPath(contextPath);
           cookie.setMaxAge(expiredSeconds);
           response.addCookie(cookie);
           return "redirect:/index";
       }
       else
       {
           model.addAttribute("usernameMsg",map.get("usernameMsg"));
           model.addAttribute("passwordMsg",map.get("passwordMsg"));
           return "/site/login";
       }
    }

    @GetMapping("/logout")//获取名为ticket cookie的值
    public String logout(@CookieValue("ticket") String ticket)
    {
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }
}

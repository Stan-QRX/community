package com.nowcoder.community.controller;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.sun.xml.internal.ws.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private DiscussPostRepository discussRepository;
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private CommentService commentService;

    @GetMapping("/")
    public String Root()
    {
        return "forward:/index";
    }

    // 查询所有帖子 在首页显示
    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page,@RequestParam(name="orderMode",defaultValue = "0") int orderMode) {
        // 方法调用之前,SpringMVC会自动实例化（调用set方法）Model和Page，当然只是对象，并将Page注入Model.
        // 所以,在thymeleaf中可以直接访问Page对象中的数据.若调用对象的非成员变量，会使用Java代码的get方法的返回值

        page.setRows(discussPostService.findDiscussPostRows(0)); // 因为要计算末页 需要rows
       /*  <li class="page-item">
                <a class="page-link" th:href="@{${page.path}(current=${page.total})}">末页</a>
            </li>*/
        page.setPath("/index?orderMode="+orderMode);
//          有默认值，可以通过get方法计算得出

        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(),orderMode);
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        //System.out.println("page current hou"+page.getTotal()+"!!!!!!!!!!!!!!!!!!!!!");
//        System.out.println("homeConteooller");
//        System.out.println(list);
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
//                System.out.println("post:"+post+"!!!!!!"+post.getId());
                map.put("post", post);

                User user = userService.findUserById(post.getUserId());
               // System.out.println(user.getHeaderUrl()+"!!!!!!");
                map.put("user", user);

                long likeCount=likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount",likeCount);

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("orderMode",orderMode);

   //    discussRepository.saveAll(discussPostMapper.selectDiscussPosts(0,0,173,0));
        System.out.println("es ok!!!!!!!!");
     //   System.out.println("code wancheng!!!");
        return "/index";
    }


    //  针对error的映射会直接打到资源中error.html中
//    @RequestMapping(path = "/error", method = RequestMethod.GET)
//    public String getErrorPage() {
//        return "/error/500";
//    }
//    @RequestMapping(path = "/error", method = RequestMethod.GET)
//    public String getErrorPage() {
//        System.out.println("500错误错误错误错误!!!!!!");
//        System.out.println("500错误错误错误错误!!!!!!");
//        System.out.println("500错误错误错误错误!!!!!!");
//        return "/error/500";
//    }

    @RequestMapping(path = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        System.out.println("404!!!!!!");
        return "/error/404";
    }
}

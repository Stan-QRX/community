package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.*;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;

import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    private static final Logger logger= LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private FollowService followService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private DiscussPostService discussPostService;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    String contextPath;

    @Autowired
    private HostHolder hostHolder;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${quniu.bucket.header.url}")
    private String headerBucketUrl;

//    @LoginRequired
//    @GetMapping("/setting")
//    public String getSettingPage(){
//        return "/site/setting";
//    }

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(Model model) {
        // ??????????????????
        String fileName = CommunityUtil.generateUUID();
        // ??????????????????
        StringMap policy = new StringMap();
        policy.put("returnBody", CommunityUtil.getJSONString(0));
        // ??????????????????
        Auth auth = Auth.create(accessKey, secretKey);
        // 3600s
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);
///     ???????????????????????????
        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);
        return "/site/setting";
    }

    // ??????????????????
    @RequestMapping(path = "/header/url", method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return CommunityUtil.getJSONString(1, "?????????????????????!");
        }

        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeader(hostHolder.getUser().getId(), url);

        return CommunityUtil.getJSONString(0);
    }

    // ??????
      //??????????????????
     @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage, Model model)
    {
        if(headerImage==null)
        {
            model.addAttribute("error","???????????????!");
            return "/site/setting";
        }

        String fileName=headerImage.getOriginalFilename();
        String suffix=fileName.substring(fileName.lastIndexOf("."));//??????????????????
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","??????????????????!");
            return "/site/setting";
        }

        //?????????????????????
        fileName= CommunityUtil.generateUUID()+suffix;
        File dest=new File(uploadPath+"/"+fileName);
        try {
            // ??????????????????????????????????????????
            headerImage.transferTo(dest);
        } catch (IOException e) {
          logger.error("??????????????????"+e.getMessage());
          throw new RuntimeException("??????????????????????????????????????????");
        }
        // ?????????????????????????????????
        User user=hostHolder.getUser();
        String headerurl=domain+contextPath+"/user/header/"+fileName;
        userService.updateHeader(user.getId(),headerurl);
        return "redirect:/index";
    }

     // ????????????
    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // ???????????????????????????
        fileName = uploadPath + "/" + fileName;
        // ????????????
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // ????????????
        try (
                InputStream fis = new FileInputStream(fileName);
        ) {
            OutputStream os = response.getOutputStream();
            // 1024?????? = 1M; byte?????????????????????
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("?????????????????????" + e.getMessage());
        }
    }

    // ???????????? ?????????????????????
    @GetMapping("/profile/{userId}")
    public String getProFilePage(@PathVariable("userId") int userId,Model model)
    {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("??????????????????!");
        }

        // ??????
        model.addAttribute("user", user);
        // ????????????
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // ????????????
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // ????????????
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // ???????????????
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }

    //???????????????????????????
    @RequestMapping(path = "/mypost/{userId}", method = RequestMethod.GET)
    public String toMyPost(@PathVariable("userId") int userId, Model model, Page page,
                           @RequestParam(name = "infoMode", defaultValue = "1") int infoMode) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("??????????????????!");
        }
        model.addAttribute("user", user);

        // ??????????????????
        page.setLimit(5);
        page.setRows(discussPostService.findDiscussPostRows(user.getId()));
        page.setPath("/user/mypost/"+userId);


        // ?????????????????????????????? ?????????????????? ??????
        List<DiscussPost> discussPosts = discussPostService.selectDiscussPostsByTime(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> list = new ArrayList<>();
        if (discussPosts != null) {
            for (DiscussPost post : discussPosts) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                // ????????????
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);

                list.add(map);
            }
            model.addAttribute("discussPosts", list);
        }
        // ????????????
        int postCount = discussPostService.findDiscussPostRows(user.getId());
        model.addAttribute("postCount", postCount);
        model.addAttribute("infoMode", infoMode);

        return "site/my-post";
    }

    //???????????????????????????
    @RequestMapping(path = "/mycomment/{userId}", method = RequestMethod.GET)
    public String toMyReply(@PathVariable("userId") int userId,Model model, Page page,
                            @RequestParam(name = "infoMode", defaultValue = "2") int infoMode) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("??????????????????!");
        }
        model.addAttribute("user", user);

        // ??????????????????
        page.setLimit(5);
        page.setRows(commentService.findCommentCountById(user.getId()));
        page.setPath("/user/mycomment/"+userId);

        // ??????????????????????????????????????? (sql: entity_type = 1)
        List<Comment> comments = commentService.
                findCommentsByUserId(user.getId(),page.getOffset(), page.getLimit());
        List<Map<String, Object>> list = new ArrayList<>();
        if (comments != null) {
            for (Comment comment : comments) {
                Map<String, Object> map = new HashMap<>();
                map.put("comment", comment);

                // ???????????? id ???????????????????????????
                String discussPostTitle = discussPostService.selectDiscussPostById(comment.getEntityId()).getTitle();
                map.put("discussPostTitle", discussPostTitle);

                list.add(map);
            }
            model.addAttribute("comments", list);
        }

        // ???????????????
        int commentCount = commentService.findCommentCountById(user.getId());
        model.addAttribute("commentCount", commentCount);
        model.addAttribute("infoMode", infoMode);

        return "site/my-reply";
    }
}

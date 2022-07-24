package com.nowcoder.community.controller;

import com.nowcoder.community.Event.EventProducer;
import com.nowcoder.community.entity.*;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title,String content)
    {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "你还没有登录哦!");
        }

        if(StringUtils.isBlank(title) || StringUtils.isBlank(content)){
            return CommunityUtil.getJSONString(1, "标题或内容不能为空!");
        }

        DiscussPost post = new DiscussPost();
        //其他不设置的属性，默认值为0
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());

        discussPostService.addDiscussPost(post);

        // 触发发帖事件 提交帖子到es服务器上
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);

        // 计算帖子分数。Set不允许重复数据
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, post.getId());

        return CommunityUtil.getJSONString(0, "发布成功!");
    }

    /* 帖子以及帖子的评论详情 */
    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model,Page page) {
        // 帖子

        DiscussPost post = discussPostService.selectDiscussPostById(discussPostId);
        model.addAttribute("post",post);
        // 作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        //  点赞
        long likeCount=likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
        model.addAttribute("likeCount",likeCount);

        //  点赞状态 根据用户是否登录判断
        int likeStatus=hostHolder.getUser()==null ? 0:
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_POST,post.getId());
        model.addAttribute("likeStatus",likeStatus);

        //  该帖子的评论 分页
        page.setLimit(5);
        page.setPath("/discuss/detail/"+discussPostId);
        page.setRows(post.getCommentCount());

        // 评论: 给帖子的评论
        // 回复: 给评论的评论
        // 评论列表 commentList找到帖子的评论，commentVoList是对帖子评论的具体信息进行查找

        List<Comment> commentList=commentService.findCommentsByEntity
                (CommunityConstant.ENTITY_TYPE_POST,post.getId(),page.getOffset(),page.getLimit());

        // 评论vo列表
        List<Map<String,Object>> commentVoList=new ArrayList<>();
        if(commentList!=null)
        {
            for(Comment comment:commentList)
            {
                Map<String,Object> commentVo=new HashMap<>();
              /*  评论*/
                System.out.println("评论内容："+comment.getContent());
                commentVo.put("comment",comment);
            /*    作者*/
                commentVo.put("user",userService.findUserById(comment.getUserId()));
                //  点赞
                likeCount=likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeCount",likeCount);
                //  点赞状态 根据用户是否登录判断
                likeStatus=hostHolder.getUser()==null ? 0:
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeStatus",likeStatus);
                // 给此评论的回复列表  没分页
                List<Comment> replyList=commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT,comment.getId(),0,Integer.MAX_VALUE);

                // 回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        // 回复
                        replyVo.put("reply", reply);
                        System.out.println("回复："+reply.getContent());
                        // 作者
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        //  点赞
                        likeCount=likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,reply.getId());
                       replyVo.put("likeCount",likeCount);
                        //  点赞状态 根据用户是否登录判断
                        likeStatus=hostHolder.getUser()==null ? 0:
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,reply.getId());
                       replyVo.put("likeStatus",likeStatus);
                        // 回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);

                // 注意是帖子下面的评论 回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments",commentVoList);

        return "/site/discuss-detail";
    }

    // 置顶
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id) {
        discussPostService.updateType(id, 1);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    // 取消置顶
    @RequestMapping(path = "/untop", method = RequestMethod.POST)
    @ResponseBody
    public String setUnTop(int id) {
        discussPostService.updateType(id, 0);

        // 触发发帖事件。因为要将数据存到ES中，这样用ES就可以进行搜索
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    // 加精
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id) {
        discussPostService.updateStatus(id, 1);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

//        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);

        return CommunityUtil.getJSONString(0);
    }

    // 取消加精
    @RequestMapping(path = "/unwonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setUnWonderful(int id) {
        discussPostService.updateStatus(id, 0);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    // 删除
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id)  {
        int tag = 0;
        List<DiscussPost> list = (List<DiscussPost>) redisTemplate.opsForValue().get("discussPost1");
       for (DiscussPost discussPost : list) {
           if (discussPost.getId() == id )
           {
               list.remove(discussPost); //删除缓存
               redisTemplate.opsForValue().set("discussPost1",list);
               tag =1;
               break;
           }
       }
        discussPostService.updateStatus(id, 2);  // 更新数据库  很快 0.03s
       if (tag == 1 && (int)redisTemplate.opsForValue().get("quartz") == 1) {
           while((int)redisTemplate.opsForValue().get("quartz") == 1) {
               try {
                   Thread.sleep(200);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }
           redisTemplate.opsForValue().set("discussPost1",list);
       }


        // 触发删帖事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
       eventProducer.fireEvent(event);


        return CommunityUtil.getJSONString(0);
    }
}
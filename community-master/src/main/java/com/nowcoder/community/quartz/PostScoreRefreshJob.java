package com.nowcoder.community.quartz;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PostScoreRefreshJob implements Job, CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private DiscussPostService discussPostService;

    // 帖子列表缓存。缓存都是按key缓存value
    private LoadingCache<String, List<DiscussPost>> postListCache;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    // Community纪元
    private static final Date epoch;

    //初始化Community纪元常量
    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2021-01-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化Community纪元失败!", e);
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("QQQ");
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

//        if (operations.size() == 0) {
//            logger.info("[任务取消] 没有需要刷新的帖子!");
//
//        }

        logger.info("[任务开始] 正在刷新帖子分数: " + operations.size());
        while (operations.size() > 0) {
            // 从有改变了分数的集合中取出一个，更新帖子分数
            this.refresh((Integer) operations.pop());
        }
        logger.info("[任务结束] 帖子分数刷新完毕!");

        redisTemplate.opsForValue().set("quartz",1);
        List<DiscussPost> list=new ArrayList<>();
        Map<String, List<DiscussPost>> map=new HashMap<>();
        list=discussPostMapper.selectDiscussPosts(0, 0,10, 1);
        map.put(0+"+"+10,list);
        redisTemplate.opsForValue().set("discussPost1",list);
        redisTemplate.opsForValue().set("quartz",0);
        // put 也可以
     //  redisTemplate.opsForHash().putAll("discussPost",map);
    }

    private void refresh(int postId) {
        DiscussPost post = discussPostService.selectDiscussPostById(postId);

        if (post == null) {
            logger.error("该帖子不存在: id = " + postId);
            return;
        }

        // 是否精华
        boolean wonderful = post.getStatus() == 1;
        // 评论数量
        int commentCount = post.getCommentCount();
        // 帖子点赞数量(不包含帖子中的评论)
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        // 计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        // 分数 = 帖子权重 + 距离天数  log函数特点：随着x增加，y增加越来越慢
        //getTime()得到long型，越新发布的帖子越有可能成为热帖
        // log10(精华分+评论数*10+点赞数*2）+（发布时间-牛客纪元）
        double score = Math.log10(Math.max(w, 1))
                + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
        // 更新帖子分数
        discussPostService.updateScore(postId, score);
        // 同步搜索数据
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);
    }

}

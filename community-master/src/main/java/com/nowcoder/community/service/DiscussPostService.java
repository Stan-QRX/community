package com.nowcoder.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;
    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    // Caffeine核心接口: Cache, LoadingCache, AsyncLoadingCache

    // 帖子列表缓存。缓存都是按key缓存value
    private LoadingCache<String, List<DiscussPost>> postListCache;

    // 帖子总数缓存
    private LoadingCache<Integer, Integer> postRowsCache;
//
//    @PostConstruct
//    public void init() {
//        // 初始化帖子列表缓存    注意缓存有过期时间的
//        postListCache = Caffeine.newBuilder()
//                .maximumSize(maxSize)
//                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
//                .build(new CacheLoader<String, List<DiscussPost>>() {
//                    @Nullable
//                    @Override
//                    public List<DiscussPost> load(@NonNull String key) throws Exception {
//                        if (key == null || key.length() == 0) {
//                            throw new IllegalArgumentException("参数错误!");
//                        }
//
//                        String[] params = key.split(":");
//                        if (params == null || params.length != 2) {
//                            throw new IllegalArgumentException("参数错误!");
//                        }
//                        System.out.println("初始化帖子列表缓存  init方法！！！！！！");
//                        int offset = Integer.valueOf(params[0]);
//                        int limit = Integer.valueOf(params[1]);
//
//                       //  二级缓存: Redis -> mysql
//                        List<DiscussPost> list=new ArrayList<>();
//                        // redis中无key返回null
//                        System.out.println("redis get :"+redisTemplate.opsForHash().get("discussPost",offset+"+"+limit));
//
//                        // 从 redis 中查出
//                        if(redisTemplate.opsForHash().hasKey("discussPost",offset+"+"+limit))
//                        {
//                            List<String> list1=new ArrayList<>();
//                            System.out.println("从 redis中查询！！！");
//                            list1.add(offset+"+"+limit);
//                            // 指定 key value 返回值
////                            System.out.println(redisTemplate.opsForHash().get("discussPost",offset+"+"+limit));
//                            // 用multiGet 注意会返回 [[]]]
//                        //  return  redisTemplate.opsForHash().multiGet("discussPost",list1);
//                           return (List<DiscussPost>) redisTemplate.opsForHash().get("discussPost",offset+"+"+limit);
//                        }
//
//                        logger.debug("load post list from DB.");
//                        System.out.println("从 数据库 中查询！！！");
//                        // 从数据库中查出
//                        Map<String,List<DiscussPost>> map=new HashMap<>();
//                      list=discussPostMapper.selectDiscussPosts(0, offset, limit, 1);
//                        map.put(offset+"+"+limit,list);
//                        // put 也可以
//                        redisTemplate.opsForHash().putAll("discussPost",map);
//
//                        redisTemplate.expire("discussPost",160,TimeUnit.SECONDS);
//                        return list;
//                    }
//                });
//        // 初始化帖子总数缓存
//        postRowsCache = Caffeine.newBuilder()
//                .maximumSize(maxSize)
//                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
//                .build(new CacheLoader<Integer, Integer>() {
//                    @Nullable
//                    @Override
//                    public Integer load(@NonNull Integer key) throws Exception {
//                        logger.debug("load post rows from DB.");
//                        return discussPostMapper.selectDiscussPostRows(key);
//                    }
//                });
//    }

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit,int orderMode) {
        //注释掉该段代码，表示不使用postListCache缓存
        if (userId == 0 && orderMode == 1) {
            // 从缓存返回结果  有缓存，直接返回   无缓存调用  postListCache的load方法，从redis或数据库中查询出结果添加到缓存中，并返回
//            System.out.println("get获取缓存！！！！！！");
//         // return discussPostMapper.selectDiscussPosts(0, offset, limit, 1);
//          return postListCache.get(offset + ":" + limit);
//            if(!redisTemplate.opsForHash().hasKey("discussPost",offset+"+"+limit))
//            {
//                System.out.println("错误！redis缓存为空");
//            return null;
//            }
            System.out.println("正常！redis有缓存！！");
//            if(Math.random()>0.5)
            List<DiscussPost> list = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                DiscussPost post = (DiscussPost) redisTemplate.opsForValue().get( "hotDiscussPost"+i);
                if (post != null)
                    list.add(post);
            }

                return list;
          // return discussPostMapper.selectDiscussPosts(userId, offset, limit,orderMode);

      //      return (List<DiscussPost>) redisTemplate.opsForHash().get("discussPost",0+"+"+10);
        }
//        System.out.println("数据库查询");
//        logger.debug("load post list from DB.");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit,orderMode);
    }

    public int findDiscussPostRows(int userId) {
//        if (userId == 0) {
//            return discussPostMapper.selectDiscussPostRows(userId);
//        }

        logger.debug("load post rows from DB.");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public List<DiscussPost> selectDiscussPostsByTime(int userId, int offset, int limit)
    {
        return discussPostMapper.selectDiscussPostsByTime(userId,offset,limit);
    }


    public int addDiscussPost(DiscussPost post) {
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 转义HTML标记,不会将标签识别出来。过滤标签 把html的标签特殊字符转换成普通字符
      /*  post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));*/
        // 过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        return discussPostMapper.insertDiscussPost(post);
    }

    public DiscussPost selectDiscussPostById(int id)
    {
        return discussPostMapper.selectDiscussPostById(id);
//    DiscussPost dis=discussPostMapper.selectByPrimaryKey(id); 会报错且status != 2

    }

    public int updateCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id, type);
    }

    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }

    public int updateScore(int id, double score) {
        return discussPostMapper.updateScore(id, score);
    }

}

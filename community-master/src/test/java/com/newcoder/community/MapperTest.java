package com.newcoder.community;

import com.nowcoder.community.CommunityApplication;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private DiscussPostService discussPostService;

    @Test
    public void testSelectPosts() {
        Map<String, Object> map = new HashMap<>();
        System.out.println(map);
        if(map==null || map.isEmpty())
            System.out.println("!!!");
//        List<DiscussPost> list = discussPostService.findDiscussPosts(0, 10, 10);
//        List<Map<String, Object>> discussPosts = new ArrayList<>();int i=0;
//        if (list != null) {
//            for (DiscussPost post : list) {i++;
//                Map<String, Object> map = new HashMap<>();
//                map.put("post", post);
//                User user = userService.findUserById(post.getUserId());
//                System.out.println(user.getHeaderUrl()+"!!!!!!");
//                map.put("user", user);
//                discussPosts.add(map);
//            }
//        }
      /*  List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0, 0, 10);
        for(DiscussPost post : list) {
            System.out.println("!!!!");
            System.out.println(post);
        }

        int rows = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(rows);*/
    }
}

package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface DiscussPostMapper  {
    /**
     * @param offset 本页起始行的行号 limit 一页的行数
     * @return 
     */
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit,int orderMode);

    List<DiscussPost>  selectDiscussPostsByTime(int userId, int offset, int limit);

    // @Param注解用于给参数取别名,
    // 如果只有一个参数,并且在<if>里使用,则必须加别名.
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(@Param("id")int id);

    int updateCommentCount(@Param("id")int id, @Param("commentCount")int commentCount);

    int updateType(int id, int type);

    int updateStatus(int id, int status);

    int updateScore(int id, double score);

}

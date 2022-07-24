package com.nowcoder.community.dao;


import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
//弃用，因为重构代码将用户登录信息存到redis中，为了提高效率
@Deprecated
public interface LoginTicketMapper {

    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")//自动生成主键
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({
            "select id,user_id,ticket,status,expired ",
            "from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(@Param("ticket")String ticket);

    @Update({
            "<script>",  //必须加这个标签（动态标签）
            "update login_ticket set status=#{status} where ticket=#{ticket} ",
            "<if test=\"ticket!=null\"> ",  // ""中有"" \"为转义字符
            "and 1=1 ",
            "</if>",
            "</script>"
    })
    int updateStatus(@Param("ticket")String ticket, @Param("status")int status);

}

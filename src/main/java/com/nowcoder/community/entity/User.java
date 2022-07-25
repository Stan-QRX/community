package com.nowcoder.community.entity;

import lombok.Data;

import java.util.Date;
@Data
public class User {

    private int id;
    private String username;
    /* 数据库和JavaBean中密码为CommunityUtil.md5(user.getPassword() + user.getSalt())*/
    private String password;
   /* 密钥，加强密码强度*/
    private String salt;
    private String email;
   /* 0-普通用户; 1-版主; 2-管理员;*/
    private int type;
    /*0-未激活; 1-已激活;*/
    private int status;
  /*  激活码  uuid*/
    private String activationCode;
    /*头像地址*/
    private String headerUrl;
    private Date createTime;



}

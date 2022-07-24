package com.nowcoder.community.entity;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;

@Data
@Document(indexName = "discusspost", type = "_doc", shards = 6, replicas = 3)//  同es建立映射
public class DiscussPost implements Serializable {

    private static final long serialVersionUID = 9050024298903124104L;

    @Id
    private int id;

    @Field(type = FieldType.Integer)
    private int userId;

    // 存储分词器：analyzer = "ik_max_word" -> 尽可能
    // ik_max_word -> 尽可能拆分出更多的词，比如互联网校招=>[互联,联网,互联网，网校，校招]
    // 仅按照需求分词
    @Field(type = FieldType.Text,analyzer = "ik_max_word",searchAnalyzer = "ik_smart")
    private String title;

    @Field(type = FieldType.Text,analyzer = "ik_max_word",searchAnalyzer = "ik_smart")
    private String content;

    /**
     * 0-普通; 1-置顶;
     */
    @Field(type = FieldType.Integer)
    private int type;

    /**
     * 0-正常; 1-精华; 2-拉黑;
     */
    @Field(type = FieldType.Integer)
    private int status;

    @Field(type = FieldType.Date)
    private Date createTime;

    @Field(type = FieldType.Integer)
    private int commentCount;

    @Field(type = FieldType.Double)
    private Double score;
}
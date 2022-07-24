package com.newcoder.community;

import com.nowcoder.community.CommunityApplication;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
@RunWith(SpringRunner.class)
public class ElasticsearchTests {
    @PostConstruct
    public void init(){
        // 解决netty启动冲突问题
        // 从Netty4Utils.setAvailableProcessors()找到的解决办法
        System.getProperty("es.set.netty.runtime.available.processors", "false");
    }
    @Autowired
    private DiscussPostMapper discussPostMapper;

    // Repository接口操作es
    // 由spring自动生成接口的实现类
    @Autowired
    private DiscussPostRepository discussRepository;

    @Autowired
    private ElasticsearchTemplate elasticTemplate;

    // ES添加单条数据
    @Test
    public void testInsert(){
        // es存储存储实体记录
        discussRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    // ES添加多条数据
    @Test
    public void testInsertList(){
        // es存储存储实体记录
        // 插入101的数据到es中
       discussRepository.saveAll(discussPostMapper.selectDiscussPosts(101,0,100,0));
//        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(102,0,100));
//        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(103,0,100));
//        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(111,0,100));
//        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(112,0,100));
//        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(131,0,100));
//        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(132,0,100));
//        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(133,0,100));
//        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(134,0,100));
    }

    // ES修改数据
    @Test
    public void testUpdate(){
        DiscussPost post = discussPostMapper.selectDiscussPostById(231);
        post.setContent("我是新手，使劲灌水");
        discussRepository.save(post);
    }
//
//    // 删除
    @Test
    public void testDelete(){
//        discussRepository.deleteById(231);
        discussRepository.deleteAll();
    }

    // 搜索
    @Test
    public void testSearchByRepository(){
        // SearchQuery是一个接口
        // NativeSearchQuery是其子实现类
        // NativeSearchQueryBuilder是一个工具类
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                // .withQuery((QueryBuilders.multiMatchQuery("关键字","字段1","字段2"))构造搜索条件
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))
                // .withSort(SortBuilders.fieldSort("排序字段").order(排序方式))构造排序条件
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // 添加分页条件 withPageable(PageRequest.of(起始页,每页条数))
                .withPageable(PageRequest.of(0,10))
                // 高亮显示
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                        // .build()构造查询器
                ).build();

        // discussRepository.search(searchQuery)的底层调用
        // elasticTemplate.queryForPage(searchQuery,class,SearchResultMapper)
        // 底层获取到了高亮显示的值，但是没有返回

        // 可以将Page看做一个集合
        Page<DiscussPost> page = discussRepository.search(searchQuery);
        // 总的元素个数
        System.out.println(page.getTotalElements());
        // 总页数
        System.out.println(page.getTotalPages());
        // 当前是第多少条记录
        System.out.println(page.getNumber());
        // 当前页
        System.out.println(page.getSize());
        for (DiscussPost post : page) {
            System.out.println(post);
        }
    }

    // 搜索
    @Test
    public void testSearchByTemplate(){
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                // .withQuery((QueryBuilders.multiMatchQuery("关键字","字段1","字段2"))构造搜索条件
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))
                // .withSort(SortBuilders.fieldSort("排序字段").order(排序方式))构造排序条件
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // 添加分页条件 withPageable(PageRequest.of(起始页,每页条数))
                .withPageable(PageRequest.of(0,10))
                // 高亮显示
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                        // .build()构造查询器
                ).build();
        Page<DiscussPost> page = elasticTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> aClass, Pageable pageable) {
                // 得到命中的数据
                SearchHits hits = response.getHits();
                if (hits.getTotalHits()<=0){
                    return null;
                }
                List<DiscussPost> list = new ArrayList<>();
                // hit是封装json格式的数据map
                for (SearchHit hit : hits) {
                    DiscussPost post = new DiscussPost();
                    String id = hit.getSourceAsMap().get("id").toString();
                    post.setId(Integer.valueOf(id));
                    String userId = hit.getSourceAsMap().get("userId").toString();
                    post.setUserId(Integer.valueOf(userId));

                    // 原始的title
                    String title = hit.getSourceAsMap().get("title").toString();
                    post.setTitle(title);

                    // 原始的content
                    String content = hit.getSourceAsMap().get("content").toString();
                    post.setContent(content);

                    String status = hit.getSourceAsMap().get("status").toString();
                    post.setStatus(Integer.valueOf(status));

                    String createTime = hit.getSourceAsMap().get("createTime").toString();
                    // createTime是一个Long类型的字符串
                    post.setCreateTime(new Date(Long.valueOf(createTime)));

                    String commentCount = hit.getSourceAsMap().get("commentCount").toString();
                    post.setCommentCount(Integer.valueOf(commentCount));

                    // 处理高亮显示的结果
                    // 获取高亮显示的内容
                    HighlightField titleField = hit.getHighlightFields().get("title");
                    if (titleField!=null){
                        // 覆盖之前的标题
                        // titleField.getFragments()返回一个数组，因为一段文本中可能有多个关键字
                        post.setTitle(titleField.getFragments()[0].toString());
                    }

                    HighlightField contentField = hit.getHighlightFields().get("content");
                    if (contentField != null) {
                        post.setContent(contentField.getFragments()[0].toString());
                    }

                    list.add(post);
                }

                // AggregatedPageImpl是AggregatedPage的实现类
                // [数据集合,pageable,记录总数,response.getAggregations(),response.getScrollId(), hits.getMaxScore()]
                return new AggregatedPageImpl(list, pageable,
                        hits.getTotalHits(), response.getAggregations(),
                        response.getScrollId(), hits.getMaxScore());
            }


            public <T> T mapSearchHit(SearchHit searchHit, Class<T> aClass) {
                return null;
            }
        });

        // 总的元素个数
        System.out.println(page.getTotalElements());
        // 总页数
        System.out.println(page.getTotalPages());
        // 当前是第多少条记录
        System.out.println(page.getNumber());
        // 当前页
        System.out.println(page.getSize());
        for (DiscussPost post : page) {
            // 返回的结果只包含关键词的一小段
            System.out.println(post);
        }
    }
}
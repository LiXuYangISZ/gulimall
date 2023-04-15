package com.atguigu.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import com.netflix.ribbon.proxy.annotation.Var;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.Request;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.IOException;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {

    @Autowired
    RestHighLevelClient client;

    @Test
    public void contextLoads() {
        System.out.println(client);
    }

    /**
     * 测试存储数据到es：https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.4/java-rest-high-create-index.html
     * 更新也可以
     */
    @Test
    public void testIndex() throws IOException {
        IndexRequest request = new IndexRequest("users");
        request.id("2");
        User user = new User();
        user.setUserName("zhangsan");
        user.setAge(23);
        user.setGender("男");
        String userStr = JSON.toJSONString(user);
        request = request.source(userStr, XContentType.JSON);
        // 执行操作
        IndexResponse response = client.index(request, GulimallElasticSearchConfig.COMMON_OPTIONS);
        // 提取有用的响应数据
        System.out.println(response);
    }

    @Test
    public void testExist() throws IOException {
        GetIndexRequest request = new GetIndexRequest("users");
        boolean exists = client.indices().exists(request, GulimallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(exists);
    }

    /**
     * 测试复杂检索：https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.4/java-rest-high-search.html
     * @throws IOException
     */
    @Test
    public void searchData() throws IOException {
        // 1.创建检索请求
        SearchRequest searchRequest = new SearchRequest("bank");
        // 2.指定DSL，检索条件
        SearchSourceBuilder builder = new SearchSourceBuilder();
        // 可构建的DSL
        // builder.query();
        // builder.aggregation();
        // builder.size();
        // builder.from();

        builder.query(QueryBuilders.matchQuery("address","mill"));

        builder.aggregation(AggregationBuilders.terms("ageAGG").field("age").size(10));
        builder.aggregation(AggregationBuilders.avg("balanceAvg").field("balance"));
        System.out.println("DSL条件："+builder.toString());
        searchRequest.source(builder);

        //2、执行检索
        SearchResponse response = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        // 3.分析结果
        System.out.println(response.toString());

        //3.1 获取所有查到的数据
        SearchHits hits = response.getHits();
        for (SearchHit hit : hits.getHits()) {
            // hit.getIndex()
            // hit.getScore()
            String json = hit.getSourceAsString();
            Account account = JSON.parseObject(json, Account.class);
            System.out.println("account:"+account);
        }
        Aggregations aggregations = response.getAggregations();

        // 3.2 获取这次检索到的分析信息
        Terms ageAGG = aggregations.get("ageAGG");
        for (Terms.Bucket bucket : ageAGG.getBuckets()) {
            String key = bucket.getKeyAsString();
            System.out.println("年龄："+key+"===>"+bucket.getDocCount());
        }

        Avg balanceAvg = aggregations.get("balanceAvg");
        System.out.println("平均薪资为："+balanceAvg.getValue());

    }

}

@Data
class User{
    private String userName;
    private Integer age;
    private String gender;
}

@Data
class Account {
    private int account_number;
    private int balance;
    private String firstname;
    private String lastname;
    private int age;
    private String gender;
    private String address;
    private String employer;
    private String email;
    private String city;
    private String state;
}



package com.atguigu.gulimall.search.service.impl;

import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/4/30 11:06
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    RestHighLevelClient client;

    /**
     * @param param 检索所有参数
     * @return
     */
    @Override
    public SearchResult search(SearchParam param) {
        // 1.动态构建出查询需要的DSL语句
        SearchResult searchResult = null;
        // 2.准备检索请求
        SearchRequest searchRequest = buildSearchRequest(param);

        try {
            // 3.执行检索请求
            SearchResponse searchResponse = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

            // 4.分析相应数据封装成我们需要的格式
            searchResult = buildSearchResult(searchResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchResult;
    }

    /**
     * 准备检索请求
     * 模糊匹配 ，过滤（按照属性、分类、品牌、价格区间、库存），排序，分页，高亮，聚合分析
     * @return
     * @param param
     */
    private SearchRequest buildSearchRequest(SearchParam param) {
        // 1.构建DSL语句
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 2.构建bool查询语句
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        /**
         * 模糊匹配 ，过滤（按照属性、分类、品牌、价格区间、库存）
         */
        // 2.1 must-模糊匹配
        if(StringUtils.isNotBlank(param.getKeyword())){
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle",param.getKeyword()));
        }
        // 2.2 filter
        // 2.2.1 按照三级分类id查询
        if(param.getCatalog3Id()!=null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId",param.getCatalog3Id()));
        }
        // 2.2.2 按照品牌id进行查询
        if(param.getBrandId()!=null && param.getBrandId().size() > 0){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId",param.getBrandId()));
        }

        // 2.2.3 按照属性查询
        if(param.getAttrs()!=null && param.getAttrs().size() > 0){
            // 注意：为啥93、103-104要写在For里面呢，因为如果写在外边就意味着，所有的attr都要参与满足，这显然是不合理的。
            // 举例：attrs=1_麒麟9000:A16&attrs_2_16g:32g;  CPU品牌要同时满足麒麟9000或A16。且内存还要同时满足16g或32g
            // 解决方案：为每一个属性都构建一个filter，这样所有满足的都会包括进来
            for (String attrStr : param.getAttrs()) {
                BoolQueryBuilder nestedBoolQueryBuilder = QueryBuilders.boolQuery();
                //attr = 1_5寸:8寸
                String[] str = attrStr.split("_");
                // 属性id
                String attrId = str[0];
                // 属性值
                String[] attrValues = str[1].split(":");
                // TODO 这里可以尝试下链式写法
                nestedBoolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId",attrId))
                        .must(QueryBuilders.termsQuery("attrs.attrValue",attrValues));
                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs", nestedBoolQueryBuilder, ScoreMode.None);
                boolQueryBuilder.filter(nestedQueryBuilder);
            }

        }

        // 2.2.4 按照是否有库存查询【不传就是查询全部】
        if(param.getHasStock() != null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock",param.getHasStock()==1));
        }

        // 2.2.5 按照价格区间进行查询
        if(StringUtils.isNotBlank(param.getSkuPrice())){
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("skuPrice");
            // 注意 _500永远是2,500_永远是1
            String[] str = param.getSkuPrice().split("_");
            if(param.getSkuPrice().startsWith("_")){
                rangeQueryBuilder.lte(str[1]);
            }else if(param.getSkuPrice().endsWith("_")){
                rangeQueryBuilder.gte(str[0]);
            }else{
                rangeQueryBuilder.gte(str[0]).lte(str[1]);
            }
            boolQueryBuilder.filter(rangeQueryBuilder);
        }

        // 把所有查询条件拿来进行封装
        searchSourceBuilder.query(boolQueryBuilder);


        /**
         * 3.排序，分页，高亮
         */
        // 3.1排序
        if(StringUtils.isNotBlank(param.getSort())){//sort=hotScore_acs/desc
            String[] str = param.getSort().split("_");
            SortOrder order = str[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            searchSourceBuilder.sort(str[0], order);
        }

        // 3.2分页
        // pageNum 1 from 0 size 5
        // pageNum 2 from 5 size 5
        // pageNum n from:(n-1)*size
        searchSourceBuilder.from((param.getPageNum() - 1)*EsConstant.PRODUCT_PAGESIZE);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        // 3.3高亮
        if(StringUtils.isNotBlank(param.getKeyword())){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle").preTags("<b sytel='color:red'>").postTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }

        String s = searchSourceBuilder.toString();
        System.out.println("构建的DSL:"+s);

        /**
         * 聚合分析
         */
        return new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX},searchSourceBuilder);
    }

    /**
     * 把响应数据封装成我们需要的格式
     * @param searchResponse
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse searchResponse) {
        return null;
    }
}

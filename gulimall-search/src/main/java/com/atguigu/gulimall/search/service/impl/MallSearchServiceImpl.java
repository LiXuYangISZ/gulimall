package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.SkuEsModel;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.feign.ProductFeignService;
import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.aspectj.weaver.ast.Var;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Autowired
    ProductFeignService productFeignService;

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
            searchResult = buildSearchResult(searchResponse, param);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchResult;
    }

    /**
     * 准备检索请求
     * 模糊匹配 ，过滤（按照属性、分类、品牌、价格区间、库存），排序，分页，高亮，聚合分析
     *
     * @param param
     * @return
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
        if (StringUtils.isNotBlank(param.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        // 2.2 filter
        // 2.2.1 按照三级分类id查询
        if (param.getCatalog3Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        // 2.2.2 按照品牌id进行查询
        if (param.getBrandId() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("brandId", param.getBrandId()));
        }

        // 2.2.3 按照属性查询
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
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
                nestedBoolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId", attrId))
                        .must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs", nestedBoolQueryBuilder, ScoreMode.None);
                boolQueryBuilder.filter(nestedQueryBuilder);
            }

        }

        // 2.2.4 按照是否有库存查询【不传就是查询全部】
        if (param.getHasStock() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }

        // 2.2.5 按照价格区间进行查询
        if (StringUtils.isNotBlank(param.getSkuPrice())) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("skuPrice");
            // 注意 _500永远是2,500_永远是1
            String[] str = param.getSkuPrice().split("_");
            if (param.getSkuPrice().startsWith("_")) {
                rangeQueryBuilder.lte(str[1]);
            } else if (param.getSkuPrice().endsWith("_")) {
                rangeQueryBuilder.gte(str[0]);
            } else {
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
        if (StringUtils.isNotBlank(param.getSort())) {//sort=hotScore_acs/desc
            String[] str = param.getSort().split("_");
            SortOrder order = str[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            searchSourceBuilder.sort(str[0], order);
        }

        // 3.2分页
        // pageNum 1 from 0 size 5
        // pageNum 2 from 5 size 5
        // pageNum n from:(n-1)*size
        searchSourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        // 3.3高亮
        if (StringUtils.isNotBlank(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle").preTags("<b style='color:red'>").postTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }


        /**
         * 4.聚合分析
         */
        // 4.1 品牌聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);
        // 品牌聚合的子聚合
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        // 4.2 分类聚合
        TermsAggregationBuilder catelogAgg = AggregationBuilders.terms("catelog_agg").field("catalogId").size(50);
        catelogAgg.subAggregation(AggregationBuilders.terms("catelog_name_agg").field("catalogName").size(1));
        // 4.3 属性聚合
        NestedAggregationBuilder attrsAgg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(50);
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attrsAgg.subAggregation(attrIdAgg);
        // 将其加入到sourceBuilder
        searchSourceBuilder.aggregation(brandAgg);
        searchSourceBuilder.aggregation(catelogAgg);
        searchSourceBuilder.aggregation(attrsAgg);
        String s = searchSourceBuilder.toString();
        System.out.println("构建的DSL:" + s);
        return new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchSourceBuilder);
    }

    /**
     * 把响应数据封装成我们需要的格式
     *
     * @param response
     * @param param
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {
        SearchResult result = new SearchResult();

        // 1.返回的所有查询到的商品
        SearchHits hits = response.getHits();
        List <SkuEsModel> skuEsModels = new ArrayList <>();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String source = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(source, SkuEsModel.class);
                // 设置搜索关键字高亮
                if (StringUtils.isNotBlank(param.getKeyword())) {
                    String skuTitleStr = hit.getHighlightFields().get("skuTitle").fragments()[0].string();
                    skuEsModel.setSkuTitle(skuTitleStr);
                }
                skuEsModels.add(skuEsModel);
            }
        }
        result.setProducts(skuEsModels);

        // 2.当前所有商品涉及到的所有属性信息
        List <SearchResult.AttrVo> attrVos = new ArrayList <>();
        ParsedNested attrAgg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");
        if (attrIdAgg.getBuckets() != null && attrIdAgg.getBuckets().size() > 0) {
            for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
                SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
                //设置attrId
                attrVo.setAttrId(bucket.getKeyAsNumber().longValue());
                //设置attrName
                ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attr_name_agg");
                attrVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());
                //设置attrValue
                ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attr_value_agg");
                List <String> attrValues = attrValueAgg.getBuckets().stream().map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());
                attrVo.setAttrValue(attrValues);

                //加入到集合中
                attrVos.add(attrVo);
            }
        }
        result.setAttrs(attrVos);

        // 3.当前所有商品涉及到的所有品牌信息
        List <SearchResult.BrandVo> brandVos = new ArrayList <>();
        ParsedLongTerms brandAgg = response.getAggregations().get("brand_agg");
        if (brandAgg.getBuckets() != null && brandAgg.getBuckets().size() > 0) {
            for (Terms.Bucket bucket : brandAgg.getBuckets()) {
                SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
                // 设置brandId
                brandVo.setBrandId(bucket.getKeyAsNumber().longValue());
                // 设置brandImg
                ParsedStringTerms brandImgAgg = bucket.getAggregations().get("brand_img_agg");
                brandVo.setBrandImg(brandImgAgg.getBuckets().get(0).getKeyAsString());
                // 设置brandName
                ParsedStringTerms brandNameAgg = bucket.getAggregations().get("brand_name_agg");
                brandVo.setBrandName(brandNameAgg.getBuckets().get(0).getKeyAsString());
                // 添加到集合
                brandVos.add(brandVo);
            }
        }
        result.setBrands(brandVos);

        // 4.当前所有商品涉及到的所有分类信息
        List <SearchResult.CatalogVo> catalogVos = new ArrayList <>();
        ParsedLongTerms catelogAgg = response.getAggregations().get("catelog_agg");
        if (catelogAgg.getBuckets() != null && catelogAgg.getBuckets().size() > 0) {
            for (Terms.Bucket bucket : catelogAgg.getBuckets()) {
                SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
                // 设置catalogId
                catalogVo.setCatalogId(bucket.getKeyAsNumber().longValue());
                // 得到子聚合
                ParsedStringTerms catelogNameAgg = bucket.getAggregations().get("catelog_name_agg");
                // 设置catalogName
                catalogVo.setCatalogName(catelogNameAgg.getBuckets().get(0).getKeyAsString());
                catalogVos.add(catalogVo);
            }
        }
        result.setCatalogs(catalogVos);

        // 5.分页信息-页码、总记录数、总页码
        long total = hits.getTotalHits().value;
        long pages = total % EsConstant.PRODUCT_PAGESIZE == 0 ? total / EsConstant.PRODUCT_PAGESIZE : total / EsConstant.PRODUCT_PAGESIZE + 1;
        result.setTotal(total);
        result.setTotalPages(pages);
        result.setPageNum(param.getPageNum());

        // 6.构建面包屑导航功能
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            List <SearchResult.NavVo> navVos = param.getAttrs().stream().map(attr -> {
                // 6.1 分析每个attrs传过来的查询数据值
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                //attr=2_5寸:7寸
                String[] str = attr.split("_");
                // TODO 目前只支持单属性检索。后续可以扩展多属性检索
                navVo.setNavValue(str[1]);
                // 向attrId列表中加入已经作为条件的id，从而让前端不再显示该条件！！！
                result.getAttrIds().add(Long.valueOf(str[0]));
                R r = productFeignService.getAttrInfo(Long.valueOf(str[0]));
                if (r.getCode() == 0) {
                    AttrResponseVo attrVo = r.getDataByName("attr", new TypeReference <AttrResponseVo>() {
                    });
                    navVo.setNavName(attrVo.getAttrName());
                } else {
                    navVo.setNavName(str[0]);
                }
                // 2.取消了这个面包屑之后，我们要跳转到的那个地方.将请求路径中对应属性的URL置空，然后拼接成新的URL
                // http://search.gulimall.com/list.html?catalog3Id=225&attrs=15_海思 attr--》brandId=xxx
                String url = replaceQueryString(param, "attrs", attr);
                navVo.setLink("http://search.gulimall.com/list.html?" + url);
                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(navVos);
        }

        // 将品牌加入到面包屑中
        if (param.getBrandId() != null) {
            List <SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo brandNav = new SearchResult.NavVo();
            brandNav.setNavName("品牌");
            // 远程查询所有品牌
            R r = productFeignService.getBrandInfo(param.getBrandId());
            if (r.getCode() == 0) {
                BrandVo brand = r.getDataByName("brand", new TypeReference <BrandVo>() {
                });
                // TODO 一个商品只可能对应一个品牌的！！！ brandId=12:15:17 这样才可以。后续开了多选，需要修改代码逻辑
                String url = replaceQueryString(param, "brandId", brand.getBrandId() + "");
                brandNav.setNavValue(brand.getName());
                brandNav.setLink("http://search.gulimall.com/list.html?" + url);
                navs.add(brandNav);
            }
        }

        // 将分类加入到面包屑中
        if(param.getCatalog3Id()!=null){
            List <SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo categoryNav = new SearchResult.NavVo();
            categoryNav.setNavName("分类");
            // 远程查询所有品牌
            R r = productFeignService.getCatelogInfo(param.getCatalog3Id());
            if (r.getCode() == 0) {
                CategoryVo category = r.getDataByName("category", new TypeReference <CategoryVo>() {
                });
                // TODO 一个商品只可能对应一个分类的！！！ catalog3Id=12:15:17 这样才可以。后续开了多选，需要修改代码逻辑
                String url = replaceQueryString(param, "catalog3Id", category.getCatId() + "");
                categoryNav.setNavValue(category.getName());
                categoryNav.setLink("http://search.gulimall.com/list.html?" + url);
                navs.add(categoryNav);
            }
        }

        return result;
    }

    private String replaceQueryString(SearchParam param, String name, String value) {
        String url = null;
        try {
            // 进行编码
            url = URLEncoder.encode(value, "UTF-8");
            url = url.replace("+", "%20");
            url = url.replace(";", "%3B");
            //TODO 1、如果同一个属性点了多遍，会重复添加（解决办法，每次追加的时候，判断name和value，如果都相等，则不再追加）
            // 2. 路径中有太多的1=1了。建议每次拼接路径前消除一部分，最多路径中允许同时存在2个   √
            url = param.getQueryString().replace(name + "=" + url, "1=1").replaceAll("&1=1", "");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }
}

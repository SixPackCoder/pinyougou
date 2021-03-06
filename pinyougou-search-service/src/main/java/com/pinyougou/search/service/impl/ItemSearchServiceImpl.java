
package com.pinyougou.search.service.impl;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import sun.misc.ASCIICaseInsensitiveComparator;

@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    /**
     * 根据关键字搜索列表
     */
    public Map<String, Object> search(Map searchMap) {

        //关键字空格处理及关键字为空的处理
        if (searchMap.get("keywords").equals("")) {//关键字为空 直接返回空
            return null;
        }
        //如果搜索的关键字带有空格字符串 将其替换空字符串
        String keywords = (String) searchMap.get("keywords");
        String replaceStr = keywords.replace(" ", "");
        searchMap.put("keywords", replaceStr);

        //将结果以map集合的形式返回
        Map<String, Object> map = new HashMap<>();
        //1.--根据关键字查询所有商品 (结果高亮)
        map.putAll(searchList(searchMap));
        //2.--根据关键字获取商品分类列表
        List<String> categoryList = searchCategoryList(searchMap);
        map.put("categoryList", categoryList);
        //3.--查询品牌列表和规格列表
        //得到商品分类的名称
        String categoryName = (String) searchMap.get("category");
        if (!categoryName.equals("")) {//如果有分类名称
            map.putAll(searchBrandAndSpecList(categoryName));
        } else {//如果没有分类名称，按照第一个查询
            if (categoryList.size() > 0) {
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }
        return map;
    }

    /**
     * 导入item列表 到solr中
     *
     * @param list
     */
    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    /**
     * 删除数据
     *
     * @param goodsIdList
     */
    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        System.out.println("删除商品ID"+goodsIdList);
        Query query=new SimpleQuery();
        Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();

    }

    /**
     * 根据关键字搜索列表并高亮
     *
     * @param searchMap
     * @return
     */
    private Map searchList(Map searchMap) {
        Map map = new HashMap();
        HighlightQuery query = new SimpleHighlightQuery();
        //设置高亮选项 构建高亮选项对象
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");//设置高亮的域
        //设置高亮的前缀后缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        highlightOptions.setSimplePostfix("</em>");
        //为查询对象设置高亮选项
        query.setHighlightOptions(highlightOptions);
        // item_keywords:复制域 通过传过来的keywords精确查找 {"网络":""} keywords就是网络  返回的rows就是 4G 3G这些
        //------------1关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //1.2按分类筛选
        if (!"".equals(searchMap.get("category"))) {
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }


        //1.3按品牌筛选
        if (!"".equals(searchMap.get("brand"))) {
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.4过滤规格
        if (searchMap.get("spec") != null) {
            Map<String, String> specMap = (Map) searchMap.get("spec");
            for (String key : specMap.keySet()) {
                Criteria filterCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //1.5 过滤价格
        if (!"".equals(searchMap.get("price"))) {
            String priceStr = (String) searchMap.get("price");
            String[] price = priceStr.split("-");
            if (!price[0].equals("0")) {
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if (!price.equals("*")) {
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //1.6分页过滤
        Integer pageNo = (Integer) searchMap.get("pageNo");//当前页
        if (pageNo == null) { //没传就默认第一页
            pageNo = 1;
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");//每页记录数
        if (pageSize == null) { //没传就默认每页20条记录
            pageSize = 20;
        }
        query.setOffset((pageNo - 1) * pageSize);//设置从哪条记录开始查询
        query.setRows(pageSize);//每页多少条记录数

        //1.7 排序
        String sortField = (String) searchMap.get("sortField");//排序的字段
        String sortMethod = (String) searchMap.get("sortMethod");//排序的规则
        if(sortField!=null&&!sortField.equals("")){//存在排序的字段
            if (sortMethod.equals("ASC")){//升序
                Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(sort);
            }
            if (sortMethod.equals("DESC")){//降序
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }
        }








        //获得高亮页对象,包含所有的高亮记录，一条记录为一个TbItem
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //获取所有的高亮记录
        List<HighlightEntry<TbItem>> highlighted = page.getHighlighted();
        //循环高亮入口，获取每一条记录
        for (HighlightEntry<TbItem> h : highlighted) {
            TbItem item = h.getEntity();//获取记录中的实体类
            if (h.getHighlights().size() > 0 && h.getHighlights().get(0).getSnipplets().size() > 0) {
                item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));//设置高亮域的结果
            }
        }
        /**
         * h.getHighlights() 获取每条高亮记录的所有高亮域  
         * 在new HighlightOptions().addField("item_title")是添加的高亮域
         * h.getHighlights().get(0).getSnipplets()获取第一个高亮域的内容
         * h.getHighlights().get(0).getSnipplets().get(0) 一个高亮域中可能存在多值
         * 取决于solr中的配置域的是否配置了multiValued是否为true
         *
         * 以上操作是对原生内容的修改封装
         */
        //不进行如上操作，获取的是原生的对象，即是没有经过高亮处理的对象
        map.put("rows", page.getContent());
        map.put("totalPages", page.getTotalPages());//总页数
        map.put("total", page.getTotalElements());//中记录数
        return map;
    }

    /**
     * 分组查询  获取商品分类列表
     *
     * @return
     */
    private List<String> searchCategoryList(Map searchMap) {
        List<String> list = new ArrayList<>();
        Query query = new SimpleQuery("*:*");
        //根据关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));//where..
        query.addCriteria(criteria);
        //设置分组选项
        GroupOptions groupoption = new GroupOptions().addGroupByField("item_category");//group by
        query.setGroupOptions(groupoption);
        //获取分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //分组结果对象
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //分组入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for (GroupEntry<TbItem> entry : content) {
            String value = entry.getGroupValue();
            list.add(value);//将分组结果添加到集合
        }
        return list;
    }

    /**
     * 根据商品分类名称去缓存中查询商品列表和商品规格列表
     *
     * @param category
     * @return
     */
    private Map searchBrandAndSpecList(String category) {
        Map map = new HashMap();
        //通过商品分类名称获取模板id
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        //模板id存在的情况下拆执行下面的操作
        if (typeId != null) {
            //通过模板id获取商品列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList", brandList);
            //通过模板id查询规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList", specList);
        }
        return map;
    }

}

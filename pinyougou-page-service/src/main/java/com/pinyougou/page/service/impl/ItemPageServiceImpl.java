package com.pinyougou.page.service.impl;

//import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {
    @Value("${pagedir}")
    private String pagedir;
    @Autowired
    private FreeMarkerConfig freeMarkerConfig;
    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbItemMapper itemMapper;

    /**
     * 根据商品id 生成item.html 商品详情页
     *
     * @param goodsId
     * @return
     */
    @Override
    public boolean genItemHtml(Long goodsId) {
        //1.创建配置类
        Configuration configuration = freeMarkerConfig.getConfiguration();
        try {
            //加载模板
            Template template = configuration.getTemplate("item.ftl");
            //设置字符集
            configuration.setDefaultEncoding("utf-8");
            //创建数据模型
            Map dataModel = new HashMap();
            //-------1.商品主表数据
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            //-------2.商品扩展表数据
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            //存入数据模型
            dataModel.put("goods",goods);
            dataModel.put("goodsDesc",goodsDesc);

            //---------3.读取商品分类名称
            String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();//1级分类
            String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();//2
            String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();//3

            dataModel.put("itemCat1",itemCat1);
            dataModel.put("itemCat2",itemCat2);
            dataModel.put("itemCat3",itemCat3);
            //-----------4. 商品sku列表
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andGoodsIdEqualTo(goodsId);//指定spu id
            criteria.andStatusEqualTo("1");//状态要有效
            example.setOrderByClause("is_default desc");//按照状态降序，保证第一个为默认
            List<TbItem> itemList = itemMapper.selectByExample(example);
            dataModel.put("itemList",itemList);

            //创建Writer对象
            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pagedir + goodsId + ".html"),"UTF-8"));
            //输出
            template.process(dataModel, out);//输出
            //关闭writer对象  防止内存溢出
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除商品详情页
     * @param goodsIds
     * @return
     */
    @Override
    public boolean deleteItemHtml(Long[] goodsIds) {
        try {
            for (Long goodsId : goodsIds) {
                new File(pagedir+goodsId+".html").delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

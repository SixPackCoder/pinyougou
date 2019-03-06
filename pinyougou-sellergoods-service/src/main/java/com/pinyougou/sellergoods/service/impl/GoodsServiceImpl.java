package com.pinyougou.sellergoods.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojoGroup.Goods;
import jdk.nashorn.internal.runtime.JSONFunctions;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbBrandMapper brandMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbSellerMapper sellerMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        /*TbGoodsExample example = new TbGoodsExample();
        Criteria criteria = example.createCriteria();
        criteria.andIsDeleteIsNull();//查询isDelete状态为null的  因为为1的是被删除了的*/
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(Goods goods) {
        //保存goods表
        goods.getGoods().setAuditStatus("0");//设置未申请状态
        goodsMapper.insert(goods.getGoods());//保存商品SPU 并返回插入商品的id

        //保存goodsDesc表
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());//设置商品扩展的id
        goodsDescMapper.insert(goods.getGoodsDesc());//保存商品属性

        saveItemList(goods);

    }

    //无论是否启用规格  这些属性都是要保存的
    private void setItemValues(TbItem item, Goods goods) {
        //商品SPU编号
        item.setGoodsId(goods.getGoods().getId());
        //商家编号
        item.setSellerId(goods.getGoods().getSellerId());
        //商品分类编号  用第三级分类编号
        item.setCategoryid(goods.getGoods().getCategory3Id());
        //创建日期
        item.setCreateTime(new Date());
        //修改日期
        item.setUpdateTime(new Date());
        //品牌名称
        TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        item.setBrand(brand.getName());
        //分类名称
        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
        item.setCategory(itemCat.getName());
        //商家名称
        TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
        item.setSeller(seller.getNickName());
        //商品图片信息  存第一张图就可以了
        String itemImages = goods.getGoodsDesc().getItemImages();
        List<Map> maps = JSON.parseArray(itemImages, Map.class);
        if (maps.size() > 0) {
            item.setImage((String) maps.get(0).get("url"));
        }
    }

    //保存商品sku列表
    private void saveItemList(Goods goods) {
        if ("1".equals(goods.getGoods().getIsEnableSpec())) {//启用规则
            //保存item表
            List<TbItem> itemList = goods.getItemList();
            for (TbItem item : itemList) {
                //1.设置商品名称tital 三星 Galaxy S4 (I9500)16G版 皓月白 联通3G手机
                //goodName + 规格名
                String title = goods.getGoods().getGoodsName();//得到的是goodName
                Map<String, Object> specMap = JSON.parseObject(item.getSpec());//得到规格列表
                //遍历规格列表
                Set<String> strings = specMap.keySet();
                for (String key : strings) {
                    title += " " + specMap.get(key);
                }
                item.setTitle(title);
                setItemValues(item, goods);
                //存入item表
                itemMapper.insert(item);
            }
        } else {//未启用规格
            TbItem item = new TbItem();
            item.setTitle(goods.getGoods().getGoodsName());//商品KPU+规格描述串作为SKU名称
            item.setPrice(goods.getGoods().getPrice());//价格
            item.setStatus("1");//状态
            item.setIsDefault("1");//是否默认
            item.setNum(99999);//库存数量
            item.setSpec("{}");//没有启用规格 所以商品规格列表置为{}空对象
            setItemValues(item, goods);
            itemMapper.insert(item);
        }
    }

    /**
     * 修改
     */
    @Override
    public void update(Goods goods) {

        goods.getGoods().setAuditStatus("0");//经过修改的商品要重新设置状态 ==>未审核
        //更新商品基本表的数据
        goodsMapper.updateByPrimaryKey(goods.getGoods());
        //更新商品的扩展属性
        goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
        //更新商品的sku
        //先要删除原有的商品sku列表
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(goods.getGoods().getId());
        itemMapper.deleteByExample(example);
        //插入需要更新的数据
        saveItemList(goods);

    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Goods findOne(Long id) {
        Goods goods = new Goods();
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        goods.setGoods(tbGoods);
        goods.setGoodsDesc(tbGoodsDesc);

        //查询商品SKU信息
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(id);//查询条件 商品id  goodsId
        List<TbItem> itemList = itemMapper.selectByExample(example);
        goods.setItemList(itemList);
        return goods;//Mapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除  是逻辑删除 并非物理删除  只是将商品的isDelete字段改为 "1"
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setIsDelete("1");
            goodsMapper.updateByPrimaryKey(tbGoods);
        }
    }


    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        Criteria criteria = example.createCriteria();
        criteria.andIsDeleteIsNull();


        if (goods != null) {
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
                //criteria.andSellerIdLike("%" + goods.getSellerId() + "%");
                //精确查询属于自己店铺的商品  不能用模糊查询
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
            }
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
            }
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andCaptionLike("%" + goods.getCaption() + "%");
            }
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
            }
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
            }
            if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
                criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
            }

        }

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 修改商品状态 用于商品审核
     *
     * @param ids
     * @param status
     */
    @Override
    public void updateStatus(Long[] ids, String status) {
        for (Long id : ids) {
            TbGoods goods = goodsMapper.selectByPrimaryKey(id);
            goods.setAuditStatus(status);
            goodsMapper.updateByPrimaryKey(goods);
        }
    }

}

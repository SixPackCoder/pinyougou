package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;

/**
 * 品牌服务层接口
 */
public interface BrandService {
    /**
     * 返回全部品牌列表
     *
     * @return
     */
    List<TbBrand> findAll();

    /**
     * 品牌查询分页
     *
     * @param pageNum  当前页码
     * @param pageSize 每页记录数
     * @return
     */
    PageResult findPage(int pageNum, int pageSize);

    /**
     * 新建品牌
     *
     * @param tbBrand
     */
    void add(TbBrand tbBrand);

    /**
     * 修改品牌
     *
     * @param tbBrand
     */
    void update(TbBrand tbBrand);

    /**
     * 根据id查询品牌
     *
     * @param id
     * @return
     */
    TbBrand findById(Long id);

    /**
     * 删除id集合中id的所有品牌
     *
     * @param ids
     */
    void delete(Long... ids);

    /**
     * 条件搜索分页查询
     * @param tbBrand
     * @return
     */
    PageResult searchPage(TbBrand tbBrand,int pageNum, int pageSize);

}
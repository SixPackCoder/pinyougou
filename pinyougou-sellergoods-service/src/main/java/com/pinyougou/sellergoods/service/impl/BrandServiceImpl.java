package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {
    @Autowired
    private TbBrandMapper brandMapper;

    /**
     * 返回全部品牌列表
     *
     * @return
     */
    @Override
    public List<TbBrand> findAll() {
        return brandMapper.selectByExample(null);
    }

    /**
     * 品牌查询分页
     *
     * @param pageNum  当前页码
     * @param pageSize 每页记录数
     * @return
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        Page<TbBrand> page = PageHelper.startPage(pageNum, pageSize);
        page = (Page<TbBrand>) brandMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 新建品牌
     *
     * @param tbBrand
     */
    @Override
    public void add(TbBrand tbBrand) {
        brandMapper.insert(tbBrand);
    }

    /**
     * 修改品牌
     *
     * @param tbBrand
     */
    @Override
    public void update(TbBrand tbBrand) {
        brandMapper.updateByPrimaryKey(tbBrand);
    }

    /**
     * 根据id查询品牌
     *
     * @param id
     * @return
     */
    @Override
    public TbBrand findById(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    /**
     * 根据id删除品牌
     *
     * @param ids
     */
    @Override
    public void delete(Long... ids) {
        for (Long id : ids) {
            brandMapper.deleteByPrimaryKey(id);
        }
    }

    /**
     * 条件搜索分页查询
     *
     * @param tbBrand
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public PageResult searchPage(TbBrand tbBrand, int pageNum, int pageSize) {
        Page<TbBrand> page = PageHelper.startPage(pageNum, pageSize);
        TbBrandExample tbBrandExample = new TbBrandExample();
        TbBrandExample.Criteria criteria = tbBrandExample.createCriteria();
        //传过来的查询条件不为空  ==> 执行查询
        if (tbBrand != null) {
            if (tbBrand.getName() != null && (!tbBrand.getName().equals(""))) {
                criteria.andNameLike("%" + tbBrand.getName() + "%");
            }
            if (tbBrand.getFirstChar() != null && (!tbBrand.getFirstChar().equals(""))) {
                criteria.andFirstCharEqualTo(tbBrand.getFirstChar());
            }
            page = (Page<TbBrand>) brandMapper.selectByExample(tbBrandExample);
            return new PageResult(page.getTotal(), page.getResult());
        } else {
            //否则直接返回空
            return null;
        }
    }
}

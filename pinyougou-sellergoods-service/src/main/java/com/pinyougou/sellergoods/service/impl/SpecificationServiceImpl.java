package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import com.pinyougou.Specification;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationExample;
import com.pinyougou.pojo.TbSpecificationExample.Criteria;
import com.pinyougou.sellergoods.service.SpecificationService;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private TbSpecificationMapper specificationMapper;
    @Autowired
    private TbSpecificationOptionMapper specificationOptionMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbSpecification> findAll() {
        return specificationMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }


    /**
     * 增加
     */
    @Override
    public void add(Specification specification) {

        specificationMapper.insert(specification.getSpecification());

        for (TbSpecificationOption specificationOption : specification.getSpecificationOptionList()) {

            /**
             *  设置规格id 先插入规格 通过SELECT LAST_INSERT_ID() AS id 得到最后一条插入数据的id
             *  要用id将规格和规格选项两张表联系起来
             */
            specificationOption.setSpecId(specification.getSpecification().getId());
            specificationOptionMapper.insert(specificationOption);
        }
    }

    /**
     * 修改
     */
    @Override
    public void update(Specification specification) {
        //修改规格
        specificationMapper.updateByPrimaryKey(specification.getSpecification());
        /**
         * 修改规格选项
         * 1.先删除原有规格选项
         * 2.保存现有规格选项
         *  2.1设置现有规格选项的specId 为 规格的id
         *  2.2开始删除
         */
        TbSpecificationOptionExample example = new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
        criteria.andSpecIdEqualTo(specification.getSpecification().getId());
        specificationOptionMapper.deleteByExample(example);
        for (TbSpecificationOption tbSpecificationOption : specification.getSpecificationOptionList()) {
            tbSpecificationOption.setSpecId(specification.getSpecification().getId());
            specificationOptionMapper.insert(tbSpecificationOption);
        }
    }

    /**
     * 根据ID获取实体
     * 实体 = 规格 + 规格选项
     *
     * @param id
     * @return
     */
    @Override
    //返回的是一个用于修改回显的封装了规格和规格选项的实体
    public Specification findOne(Long id) {
        //查询到规格实体
        TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
        //准备根据ID查询规格选项表  获取该规格所对应的规格选项
        TbSpecificationOptionExample example = new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
        criteria.andSpecIdEqualTo(id);
        //得到规格选项
        List<TbSpecificationOption> tbSpecificationOptionList = specificationOptionMapper.selectByExample(example);
        //讲规格和规格选项分装到实体中
        Specification specification = new Specification();
        specification.setSpecification(tbSpecification);
        specification.setSpecificationOptionList(tbSpecificationOptionList);
        return specification;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            /**
             * 删除规格的同时  还要删除规格所对应的的规格选项
             */
            specificationMapper.deleteByPrimaryKey(id);
            TbSpecificationOptionExample example = new TbSpecificationOptionExample();
            TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
            criteria.andSpecIdEqualTo(id);
            specificationOptionMapper.deleteByExample(example);
        }

    }


    @Override
    public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbSpecificationExample example = new TbSpecificationExample();
        Criteria criteria = example.createCriteria();

        if (specification != null) {
            if (specification.getSpecName() != null && specification.getSpecName().length() > 0) {
                criteria.andSpecNameLike("%" + specification.getSpecName() + "%");
            }

        }

        Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public List<Map> selectOptionList() {
        return specificationMapper.selectOptionList();
    }

}

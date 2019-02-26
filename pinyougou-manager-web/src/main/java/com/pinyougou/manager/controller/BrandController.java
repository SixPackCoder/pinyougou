package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 运营商的品牌控制层
 */
@RestController
@RequestMapping("/brand")
public class BrandController {

    @Reference
    private BrandService brandService;

    /**
     * 查询所有品牌
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbBrand> findAll() {
        return brandService.findAll();
    }

    /**
     * 分页查询品牌
     *
     * @param page 当前页
     * @param size 每页的记录数
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page, int size) {
        return brandService.findPage(page, size);
    }

    /**
     * 新建品牌
     *
     * @param tbBrand
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody TbBrand tbBrand) {
        try {
            brandService.add(tbBrand);
            return new Result(true, "保存成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "保存失败!");
        }
    }

    /**
     * 根据id查询品牌
     *
     * @param id
     * @return
     */
    @RequestMapping("/findById")
    public TbBrand findById(Long id) {
        return brandService.findById(id);
    }

    /**
     * 修改品牌
     *
     * @param tbBrand
     * @return
     */
    @RequestMapping("/update")
    Result update(@RequestBody TbBrand tbBrand) {
        try {
            brandService.update(tbBrand);
            return new Result(true, "修改成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败!");
        }
    }

    /**
     * 删除
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    Result delete(Long... ids) {
        System.out.println(ids);
        try {
            brandService.delete(ids);
            return new Result(true, "删除成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败!");
        }
    }

    /**
     * 条件搜索查询  带分页
     * @param tbBrand
     * @param page
     * @param size
     * @return
     */
    @RequestMapping("/search")
    PageResult search(@RequestBody TbBrand tbBrand,int page,int size){
       return brandService.searchPage(tbBrand,page,size);
    }
}

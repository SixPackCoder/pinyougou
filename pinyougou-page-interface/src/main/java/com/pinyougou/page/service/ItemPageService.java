package com.pinyougou.page.service;

/**
 * 商品详细页接口
 */
public interface ItemPageService {
    /**
     * 根据商品id 生成item.html 商品详情页
     * @param goodsId
     * @return
     */
    public boolean genItemHtml(Long goodsId);
}

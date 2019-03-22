package com.pinyougou.pay.service;

import java.util.Map;

/**
 * 微信支付服务接口
 */
public interface WeiXinPayService {
    /**
     * 生成微信支付二维码
     * @param out_trade_no  订单号
     * @param total_fee        金额
     * @return
     */
    public Map createNative(String out_trade_no,String total_fee);

    /**
     * 查询订单支付状态
     * @param out_trade_no
     * @return
     */
    public Map queryPayStatus(String out_trade_no);
}

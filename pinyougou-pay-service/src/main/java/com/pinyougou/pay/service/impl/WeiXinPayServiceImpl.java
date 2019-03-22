package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.common.util.HttpClient;
import com.pinyougou.pay.service.WeiXinPayService;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付服务层
 */
@Service
public class WeiXinPayServiceImpl implements WeiXinPayService {

    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${partnerkey}")
    private String partnerkey;

    @Value("${notifyurl}")
    private String notifyurl;

    /**
     * 生成微信支付二维码
     *
     * @param out_trade_no 订单号
     * @param total_fee    金额
     * @return
     */
    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        //1.创建参数
        Map param = new HashMap();
        param.put("appid", appid);//应用id
        param.put("mch_id", partner);//商户号	mch_id
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串	nonce_str
        param.put("body", "Robin在线购");//商品描述	body
        param.put("out_trade_no", out_trade_no);//商户订单号	out_trade_no
        param.put("total_fee", total_fee);//总金额	total_fee
        param.put("spbill_create_ip", "127.0.0.1");//终端IP	spbill_create_ip
        param.put("notify_url", "http://www.baidu.com");//通知地址	notify_url
        param.put("trade_type", "NATIVE");//交易类型	trade_type
        //2.生成要发送的xml
        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("请求的参数:" + xmlParam);

            //3.发送请求

            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();
            //4.获得结果
            String xmlResult = httpClient.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlResult);
            System.out.println("接收到的结果: " + resultMap);

            //将结果选择性的传回去 不要的信息过滤掉 以一个新的map封装这些要川汇区的信息
            Map map = new HashMap();
            map.put("code_url", resultMap.get("code_url"));//支付地址
            map.put("out_trade_no", out_trade_no);//订单号
            map.put("total_fee", total_fee);//金额
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }

    }

    /**
     * 查询订单支付状态
     *
     * @param out_trade_no
     * @return
     */
    @Override
    public Map queryPayStatus(String out_trade_no) {
        //创建参数
        Map param = new HashMap();
        param.put("appid", appid);//公众账号ID 	appid
        param.put("mch_id", partner);//商户号	mch_id
        param.put("out_trade_no", out_trade_no);//商户订单号	out_trade_no
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串	nonce_str

        String url = "https://api.mch.weixin.qq.com/pay/orderquery";
        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            //发送数据
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();
            //返回结果
            String xmlResult = httpClient.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlResult);
            System.out.println("支付订单后返回的结果: "+resultMap);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }
}

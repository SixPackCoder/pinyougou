package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeiXinPayService;
import com.pinyougou.pojo.TbPayLog;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付控制层
 */
@RestController
@RequestMapping("/pay")
public class PayController {
    @Reference(timeout = 10000)
    private WeiXinPayService weiXinPayService;
    @Reference(timeout = 10000)
    private OrderService orderService;

    /**
     * 生成二维码
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative(){
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();//当前用户id
        TbPayLog payLog = orderService.searchPayLogFromRedis(userName);

        if (payLog!=null){
            //从支付日志中获取订单金额和支付订单id
            Map map = weiXinPayService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
            return map;
        }else {
            return new HashMap();
        }
    }

    /**
     * 查询支付状态
     * @param out_trade_no  订单号
      * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        Result result = null;
        int x = 0;
        while (true){
            Map<String,String> map = weiXinPayService.queryPayStatus(out_trade_no);
            if (map==null){
                result = new Result(false,"支付出错");
                break;
            }
            if (map.get("trade_state").equals("SUCCESS")){
                result = new Result(true,"支付成功");
                //支付成功就修改订单状态和支付日志状态
                orderService.updateOrderStatus(out_trade_no, map.get("transaction_id"));
                break;
            }
            try {
                Thread.sleep(3000);//间隔三秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;//循环一遍技术一次
            if(x>=100){ //超过5分钟
                result = new Result(false,"支付超时,二维码已失效");
                break;
            }
        }

        return result;
    }
}

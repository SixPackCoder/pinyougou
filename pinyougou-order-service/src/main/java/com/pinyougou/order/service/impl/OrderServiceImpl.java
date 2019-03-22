package com.pinyougou.order.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojoGroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.order.service.OrderService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private TbOrderMapper orderMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private TbOrderItemMapper orderItemMapper;
    @Autowired
    private TbPayLogMapper payLogMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbOrder> findAll() {
        return orderMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     * <p>
     * order_id 订单id
     * payment 实付金额。精确到2位小数;单位:元。如:200.07，表示:200元7分
     * payment_type 支付类型，1、在线支付，2、货到付款
     * post_fee 邮费。精确到2位小数;单位:元。如:200.07，表示:200元7分
     * status 状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭,7、待评价
     * create_time 订单创建时间
     * update_time 订单更新时间
     * payment_time 付款时间
     * consign_time 发货时间
     * end_time 交易完成时间
     * close_time 交易关闭时间
     * shipping_name 物流名称
     * shipping_code 物流单号
     * user_id 用户id
     * buyer_message 买家留言
     * buyer_nick 买家昵称
     * buyer_rate 买家是否已经评价
     * receiver_area_name 收货人地区名称(省，市，县)街道
     * receiver_mobile 收货人手机
     * receiver_zip_code 收货人邮编
     * receiver 收货人
     * expire 过期时间，定期清理
     * invoice_type 发票类型(普通发票，电子发票，增值税发票)
     * source_type 订单来源：1:app端，2：pc端，3：M端，4：微信端，5：手机qq端
     * seller_id          商家ID
     */
    @Override
    public void add(TbOrder order) {
        List<String> orderList = new ArrayList<>();//用于存放订单号列表
        double total_money = 0;//订单的总金额(元为单位)
        //得到购物车的数据  从redis中
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());

        for (Cart cart : cartList) {
            //订单id  雪花算法 随机
            long orderId = idWorker.nextId();
            System.out.println("sellerId:  " + order.getSellerId());
            //新建订单对象
            TbOrder tbOrder = new TbOrder();
            tbOrder.setOrderId(orderId);//订单id
            tbOrder.setUserId(order.getSellerId());//商家id
            tbOrder.setPaymentType(order.getPaymentType());//支付类型
            tbOrder.setStatus("1");//状态 1 未付款
            //create_time 订单创建时间
            tbOrder.setCreateTime(new Date());
            //update_time 订单更新时间
            tbOrder.setUpdateTime(new Date());
            //receiver_area_name 收货人地区名称(省，市，县)街道
            //receiver_mobile 收货人手机
            tbOrder.setReceiverAreaName(order.getReceiverAreaName());
            tbOrder.setReceiverMobile(order.getReceiverMobile());
            tbOrder.setReceiver(order.getReceiver());

            //循环购物车明细

			/*
			* id
			  item_id  商品id
			  goods_idSPU_ID
			  order_id订单id
			  title商品标题
			  price商品单价
			  num商品购买数量
			  total_fee商品总金额
			  pic_path商品图片地址
			  seller_id
*/
            double money = 0;
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                //id
                orderItem.setId(idWorker.nextId());
                //order_id 订单id
                orderItem.setOrderId(orderId);
                orderList.add(orderId + "");//将订单号 添加进订单列表
                //seller_id
                orderItem.setSellerId(order.getSellerId());
                //金额累加
                money += orderItem.getTotalFee().doubleValue();//金额累加
                orderItemMapper.insert(orderItem);
                total_money += money;//总的订单的金额  上面money是 每个订单的额金额
            }
            //订单金额
            tbOrder.setPayment(new BigDecimal(money));
            orderMapper.insert(tbOrder);

            if (order.getPaymentType().equals("1")) {//支付类型为1 代表微信支付的时候才产生支付日志

                TbPayLog payLog = new TbPayLog();
                payLog.setCreateTime(new Date());//订单创建时间
                payLog.setOutTradeNo(idWorker.nextId() + "");//支付订单号
                String order_List = orderList.toString().replace("[", "").replace("]", "");
                payLog.setOrderList(order_List);//订单号列表
                payLog.setPayType("1");//支付类型 1 微信
                payLog.setTotalFee((long) (total_money * 100));//总金额(分为单位)
                payLog.setUserId(order.getUserId());//用户id
                payLog.setTradeState("0");//交易状态 0 未完成
                payLogMapper.insert(payLog);//保存到支付日志
                //支付日志写入缓存
                redisTemplate.boundHashOps("payLog").put(order.getUserId(), payLog);
            }

        }
        //清除缓存中的购物车数据
        redisTemplate.boundHashOps("cartList").delete(order.getUserId());

    }


    /**
     * 修改
     */
    @Override
    public void update(TbOrder order) {
        orderMapper.updateByPrimaryKey(order);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbOrder findOne(Long id) {
        return orderMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            orderMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbOrderExample example = new TbOrderExample();
        Criteria criteria = example.createCriteria();

        if (order != null) {
            if (order.getPaymentType() != null && order.getPaymentType().length() > 0) {
                criteria.andPaymentTypeLike("%" + order.getPaymentType() + "%");
            }
            if (order.getPostFee() != null && order.getPostFee().length() > 0) {
                criteria.andPostFeeLike("%" + order.getPostFee() + "%");
            }
            if (order.getStatus() != null && order.getStatus().length() > 0) {
                criteria.andStatusLike("%" + order.getStatus() + "%");
            }
            if (order.getShippingName() != null && order.getShippingName().length() > 0) {
                criteria.andShippingNameLike("%" + order.getShippingName() + "%");
            }
            if (order.getShippingCode() != null && order.getShippingCode().length() > 0) {
                criteria.andShippingCodeLike("%" + order.getShippingCode() + "%");
            }
            if (order.getUserId() != null && order.getUserId().length() > 0) {
                criteria.andUserIdLike("%" + order.getUserId() + "%");
            }
            if (order.getBuyerMessage() != null && order.getBuyerMessage().length() > 0) {
                criteria.andBuyerMessageLike("%" + order.getBuyerMessage() + "%");
            }
            if (order.getBuyerNick() != null && order.getBuyerNick().length() > 0) {
                criteria.andBuyerNickLike("%" + order.getBuyerNick() + "%");
            }
            if (order.getBuyerRate() != null && order.getBuyerRate().length() > 0) {
                criteria.andBuyerRateLike("%" + order.getBuyerRate() + "%");
            }
            if (order.getReceiverAreaName() != null && order.getReceiverAreaName().length() > 0) {
                criteria.andReceiverAreaNameLike("%" + order.getReceiverAreaName() + "%");
            }
            if (order.getReceiverMobile() != null && order.getReceiverMobile().length() > 0) {
                criteria.andReceiverMobileLike("%" + order.getReceiverMobile() + "%");
            }
            if (order.getReceiverZipCode() != null && order.getReceiverZipCode().length() > 0) {
                criteria.andReceiverZipCodeLike("%" + order.getReceiverZipCode() + "%");
            }
            if (order.getReceiver() != null && order.getReceiver().length() > 0) {
                criteria.andReceiverLike("%" + order.getReceiver() + "%");
            }
            if (order.getInvoiceType() != null && order.getInvoiceType().length() > 0) {
                criteria.andInvoiceTypeLike("%" + order.getInvoiceType() + "%");
            }
            if (order.getSourceType() != null && order.getSourceType().length() > 0) {
                criteria.andSourceTypeLike("%" + order.getSourceType() + "%");
            }
            if (order.getSellerId() != null && order.getSellerId().length() > 0) {
                criteria.andSellerIdLike("%" + order.getSellerId() + "%");
            }

        }

        Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 通过用户id从redis查询支付日志
     *
     * @param userId
     * @return
     */
    @Override
    public TbPayLog searchPayLogFromRedis(String userId) {
        return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
    }

    /**
     * 更新订单状态
     *
     * @param out_trade_no
     * @param transaction_id 交易流水号
     */
    @Override
    public void updateOrderStatus(String out_trade_no, String transaction_id) {
        //修改支付日志的状态
        TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
        payLog.setPayTime(new Date());//支付时间
        payLog.setTradeState("1");//已支付完成
        payLog.setTransactionId(transaction_id);//交易流水号
        payLogMapper.updateByPrimaryKey(payLog);
        //修改订单的状态
        String orderList = payLog.getOrderList();//从支付日志里得到订单列表
        String[] orderIds = orderList.split(",");
        for (String orderId : orderIds) {
            TbOrder order = orderMapper.selectByPrimaryKey(Long.valueOf(orderId));//根据订单号查询订单
            if (order!=null) {
               order.setStatus("2");//已付款
                orderMapper.updateByPrimaryKey(order);
            }
        }
        redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
        //清除redis中的缓存
    }

}

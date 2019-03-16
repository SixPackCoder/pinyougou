package com.pinyougou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;
@Component
public class ItemSearchListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        System.out.println("监听接收到消息了...");
        //生产者发送的消息  spu列表  json字符串
        TextMessage textMessage = (TextMessage) message;
        try {
            String text = textMessage.getText();
            List<TbItem> itemList = JSON.parseArray(text, TbItem.class);
            for (TbItem item : itemList) {
                System.out.println(item.getId()+" "+item.getTitle());
                Map specMap = JSON.parseObject(item.getSpec());
                item.setSpecMap(specMap);//给带注解的字段赋值
            }
            itemSearchService.importList(itemList);//导入索引库
            System.out.println("成功导入到索引库...");
        } catch (JMSException e) {
            e.printStackTrace();
        }


    }
}

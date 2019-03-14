package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Component
public class PageListener implements MessageListener {
    @Autowired
    private ItemPageService itemPageService;
    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        try {
            String goodsId = textMessage.getText();
            System.out.println("接收到的消息:"+goodsId);
            boolean b = itemPageService.genItemHtml(Long.parseLong(goodsId));
            System.out.println("网页生成的结果:"+b);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}

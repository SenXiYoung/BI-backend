package com.yupi.springbootinit.bizmq;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.yupi.springbootinit.constant.BiMqConstant;

public class BiInitMain {

    public static void main(String[] args) {
        try{
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("101.33.243.202");
            factory.setUsername("senxiyoung");
            factory.setPassword("cxy110983");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(BiMqConstant.BI_EXCHANGE_NAME,"direct");

            channel.queueDeclare(BiMqConstant.BI_QUEUE_NAME,true,false,false,null);
            channel.queueBind(BiMqConstant.BI_QUEUE_NAME,BiMqConstant.BI_EXCHANGE_NAME,BiMqConstant.BI_ROUTING_KEY);
        }catch (Exception e){

        }
    }


}

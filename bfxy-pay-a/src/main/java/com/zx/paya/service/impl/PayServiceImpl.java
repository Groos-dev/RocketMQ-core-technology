package com.zx.paya.service.impl;

import com.zx.paya.entity.CustomerAccount;
import com.zx.paya.mapper.CustomerAccountMapper;
import com.zx.paya.service.PayService;
import com.zx.paya.service.provider.TransactionMessageProducer;
import com.zx.paya.utils.FastJsonConvertUtil;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * @author Mr.xin
 */
@Service
public class PayServiceImpl implements PayService {

    private static final String TOPIC = "pay-topic";
    private static final String TAG = "pay";

    @Autowired
    private CustomerAccountMapper customerAccountMapper;

    @Autowired
    private TransactionMessageProducer transactionMessageProducer;

    @Override
    public String pay(String accountId, String orderId, String userId, double money) {
        System.out.println(Thread.currentThread().getName());
        String resultMsg = null;
        // token 支付去重
        // 分布式锁保证在同一时刻只有一个线程在操作数据库
        //lock
        CustomerAccount old = customerAccountMapper.selectByPrimaryKey(accountId);
        Integer version = old.getVersion();
        BigDecimal currentBalance = old.getCurrentBalance();
        BigDecimal newBalance = currentBalance.subtract(new BigDecimal(money));
        if (newBalance.doubleValue() < 0) {
            resultMsg = "余额不足";
        } else {
            Map<String, Object> data = new HashMap<>();
            data.put("accountId", accountId);
            data.put("orderId", orderId);
            data.put("userId", userId);
            data.put("money", money);

            String messageKey = UUID.randomUUID().toString().replaceAll("-", "");
            Message message = new Message(TOPIC, TAG, messageKey, FastJsonConvertUtil.convertObjectToJSON(data).getBytes());
            try {
                CountDownLatch countDownLatch = new CountDownLatch(1);
                Map<String, Object> args = new HashMap<>();
                args.put("version", version);
                args.put("newBalance", newBalance);
                args.put("accountId", accountId);
                args.put("countDownLatch", countDownLatch);
                TransactionSendResult sendResult = transactionMessageProducer.send(message, args);
                countDownLatch.await();

                System.out.println(sendResult.getLocalTransactionState());
                if(sendResult.getSendStatus() == SendStatus.SEND_OK
                && sendResult.getLocalTransactionState() == LocalTransactionState.COMMIT_MESSAGE){
                    resultMsg = "支付成功";
                }else{
                    resultMsg = "支付失败";
                }
            } catch (MQClientException | InterruptedException e) {
                e.printStackTrace();
                resultMsg =  "支付失败";
            }
        }

        return resultMsg;
    }
}

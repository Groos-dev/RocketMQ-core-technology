package com.zx.paya.service;

/**
 * @author Mr.xin
 */
public interface PayService {
    String pay(String accountId, String orderId, String userId, double money);
}

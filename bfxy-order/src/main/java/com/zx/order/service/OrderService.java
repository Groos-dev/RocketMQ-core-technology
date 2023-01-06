package com.zx.order.service;


public interface OrderService {

    boolean createOrder(String cityId, String platformId, String userId, String supplierId, String goodsId);
}

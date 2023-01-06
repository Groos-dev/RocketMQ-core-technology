package com.zx.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.zx.order.constants.OrderStatus;
import com.zx.order.entity.Order;
import com.zx.order.mapper.OrderMapper;
import com.zx.order.service.OrderService;
import com.zx.store.service.provider.StoreServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.UUID;

/**
 * @author Mr.xin
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Reference(
            version = "1.0.0",
            interfaceName = "com.zx.store.service.provider.StoreServiceApi",
            application = "${dubbo.application.id}",
            check = false,
            retries = 0,
            timeout = 3000
    )
    private StoreServiceApi storeServiceApi;

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public boolean createOrder(String cityId, String platformId, String userId, String supplierId, String goodsId) {
        boolean flag = true;
        Order order = new Order();
        String orderId = UUID.randomUUID().toString().replace("-", "");
        order.setOrderId(orderId);
        order.setCityId(cityId);
        order.setPlatformId(platformId);
        order.setUserId(userId);
        order.setSupplierId(supplierId);
        order.setGoodsId(goodsId);
        order.setCreateBy("admin");
        order.setOrderStatus(OrderStatus.CREATED.getCode());
        order.setOrderType("0");
        order.setCreateTime(new Date(System.currentTimeMillis()));
        order.setCreateBy("admin");
        order.setRemark("");
        order.setUpdateBy("admin");
        order.setUpdateTime(new Date(System.currentTimeMillis()));

        try {
            int storeCount = storeServiceApi.selectStoreCount(supplierId, goodsId);
            if (storeCount < 0) {
                return false;
            }
            int version = storeServiceApi.selectVersion(supplierId, goodsId);
            int updateRetCount = storeServiceApi.updateStoreCountByVersion(supplierId, goodsId, version, new Date(System.currentTimeMillis()), "admin");
            if (updateRetCount == 1) {
                orderMapper.insertSelective(order);
            } else if (updateRetCount == 0) {
                flag = false;
                int currentCount = storeServiceApi.selectStoreCount(supplierId, goodsId);
                if (currentCount == 0) {
                    System.out.println("库存不足");
                } else {
                    System.out.println("乐观锁生效");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            flag = false;
            // 回滚操作
            orderMapper.deleteByPrimaryKey(orderId);
        }

        return flag;
    }
}

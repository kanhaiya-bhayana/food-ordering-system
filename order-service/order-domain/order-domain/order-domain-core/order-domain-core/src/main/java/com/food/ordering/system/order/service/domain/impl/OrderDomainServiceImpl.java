package com.food.ordering.system.order.service.domain.impl;

import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurent;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.interfaces.IOrderDomainService;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class OrderDomainServiceImpl implements IOrderDomainService {
    private static final String UTC = "UTC";
    @Override
    public OrderCreatedEvent validateAndInitiateOrder(Order order, Restaurent restaurent) {
        validateRestaurent(restaurent);
        setOrderProductInformation(order,restaurent);
        order.validateOrder();
        order.initializeOrder();
        log.info("Order with id: {} is initiated", order.getId().getValue());
        return new OrderCreatedEvent(order, ZonedDateTime.now(ZoneId.of(UTC)));
    }

    @Override
    public OrderPaidEvent payOrder(Order order) {
        order.pay();
        log.info("Order with id: {} is paid", order.getId().getValue());
        return new OrderPaidEvent(order, ZonedDateTime.now(ZoneId.of(UTC)));
    }

    @Override
    public void approveOrder(Order order) {
        order.approve();
        log.info("Order with id: {} is approved", order.getId().getValue());
    }

    @Override
    public OrderCancelledEvent cancelOrderPayment(Order order, List<String> failureMessages) {
        order.initCancel(failureMessages);
        log.info("Order payment is cancelling for order id: {}", order.getId().getValue());
        return new OrderCancelledEvent(order, ZonedDateTime.now(ZoneId.of(UTC)));
    }

    @Override
    public void cancelOrder(Order order, List<String> failureMessages) {
        order.cancel(failureMessages);
        log.info("Order with id: {} is cancelled", order.getId().getValue());
    }

    private void validateRestaurent(Restaurent restaurent){
        if (!restaurent.isActive()){
            throw new OrderDomainException("Restaurant with id " + restaurent.getId().getValue() +
                    "is currently not active!");
        }
    }

    private void setOrderProductInformation(Order order, Restaurent restaurent)
    {
        Set<Product> restaurantProducts = new HashSet<>(restaurent.getProducts());
        Set<Product> orderProducts = new HashSet<>();

        for(OrderItem orderItem: order.getItems()){
            orderProducts.add(orderItem.getProduct());
        }

        restaurantProducts.retainAll(orderProducts);

        for (Product product : restaurantProducts){
            product.updateWithConfirmedNameAndPrice(product.getName(),product.getPrice());
        }
    }
}

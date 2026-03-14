package com.dattakrupa.laundry.repository;

import com.dattakrupa.laundry.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Order ke saare items
    List<OrderItem> findByOrderId(Long orderId);

    // Sabse zyada dhule kapde
    List<OrderItem> findByItemNameContainingIgnoreCase(String itemName);
}
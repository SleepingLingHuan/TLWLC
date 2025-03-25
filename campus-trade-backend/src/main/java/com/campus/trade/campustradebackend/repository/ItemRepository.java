package com.campus.trade.campustradebackend.repository;

import java.util.List;
import com.campus.trade.campustradebackend.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByUserId(Long userId);
}
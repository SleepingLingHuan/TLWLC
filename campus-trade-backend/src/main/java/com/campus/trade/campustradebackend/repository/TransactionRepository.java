package com.campus.trade.campustradebackend.repository;

import java.util.List;
import com.campus.trade.campustradebackend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByBuyerIdOrSellerId(Long buyerId, Long sellerId);
}
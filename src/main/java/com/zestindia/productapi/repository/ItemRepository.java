package com.zestindia.productapi.repository;

import com.zestindia.productapi.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Page<Item> findByProductId(Long productId, Pageable pageable);
}

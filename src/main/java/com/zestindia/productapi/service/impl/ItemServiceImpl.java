package com.zestindia.productapi.service.impl;

import com.zestindia.productapi.dto.ItemRequest;
import com.zestindia.productapi.dto.ItemResponse;
import com.zestindia.productapi.dto.PagedResponse;
import com.zestindia.productapi.entity.Item;
import com.zestindia.productapi.entity.Product;
import com.zestindia.productapi.exception.ResourceNotFoundException;
import com.zestindia.productapi.repository.ItemRepository;
import com.zestindia.productapi.repository.ProductRepository;
import com.zestindia.productapi.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional
    public ItemResponse addItemToProduct(Long productId, ItemRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Item item = Item.builder()
                .product(product)
                .quantity(request.getQuantity())
                .build();

        Item saved = itemRepository.save(item);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ItemResponse> getItemsByProductId(Long productId, int page, int size) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Item> itemPage = itemRepository.findByProductId(productId, pageable);

        return PagedResponse.<ItemResponse>builder()
                .content(itemPage.getContent().stream().map(this::toResponse).toList())
                .pageNumber(itemPage.getNumber())
                .pageSize(itemPage.getSize())
                .totalElements(itemPage.getTotalElements())
                .totalPages(itemPage.getTotalPages())
                .last(itemPage.isLast())
                .build();
    }

    private ItemResponse toResponse(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .quantity(item.getQuantity())
                .build();
    }
}

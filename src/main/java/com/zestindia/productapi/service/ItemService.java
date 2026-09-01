package com.zestindia.productapi.service;

import com.zestindia.productapi.dto.ItemRequest;
import com.zestindia.productapi.dto.ItemResponse;
import com.zestindia.productapi.dto.PagedResponse;

public interface ItemService {

    ItemResponse addItemToProduct(Long productId, ItemRequest request);

    PagedResponse<ItemResponse> getItemsByProductId(Long productId, int page, int size);
}

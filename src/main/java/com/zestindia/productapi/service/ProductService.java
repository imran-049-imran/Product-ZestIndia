package com.zestindia.productapi.service;

import com.zestindia.productapi.dto.PagedResponse;
import com.zestindia.productapi.dto.ProductRequest;
import com.zestindia.productapi.dto.ProductResponse;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    ProductResponse getProductById(Long id);

    PagedResponse<ProductResponse> getAllProducts(String name, int page, int size, String sortBy, String direction);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);
}

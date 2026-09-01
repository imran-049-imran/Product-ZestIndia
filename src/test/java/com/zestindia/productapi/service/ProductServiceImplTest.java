package com.zestindia.productapi.service;

import com.zestindia.productapi.dto.PagedResponse;
import com.zestindia.productapi.dto.ProductRequest;
import com.zestindia.productapi.dto.ProductResponse;
import com.zestindia.productapi.entity.Product;
import com.zestindia.productapi.exception.ResourceNotFoundException;
import com.zestindia.productapi.repository.ProductRepository;
import com.zestindia.productapi.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder().id(1L).productName("Laptop").build();
    }

    @Test
    void createProduct_shouldSaveAndReturnResponse() {
        ProductRequest request = new ProductRequest("Laptop");
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.createProduct(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getProductName()).isEqualTo("Laptop");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void getProductById_whenExists_shouldReturnResponse() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1L);

        assertThat(response.getProductName()).isEqualTo("Laptop");
    }

    @Test
    void getProductById_whenNotFound_shouldThrow() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAllProducts_shouldReturnPagedResponse() {
        Page<Product> page = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);
        when(productRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        PagedResponse<ProductResponse> result = productService.getAllProducts(null, 0, 10, "id", "ASC");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void updateProduct_whenExists_shouldUpdateAndReturn() {
        ProductRequest request = new ProductRequest("Updated Laptop");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.updateProduct(1L, request);

        assertThat(response).isNotNull();
        verify(productRepository).save(product);
    }

    @Test
    void deleteProduct_whenExists_shouldDelete() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).delete(product);
    }

    @Test
    void deleteProduct_whenNotFound_shouldThrow() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

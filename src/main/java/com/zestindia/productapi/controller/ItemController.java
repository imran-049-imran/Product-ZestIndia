package com.zestindia.productapi.controller;

import com.zestindia.productapi.dto.ItemRequest;
import com.zestindia.productapi.dto.ItemResponse;
import com.zestindia.productapi.dto.PagedResponse;
import com.zestindia.productapi.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products/{productId}/items")
@RequiredArgsConstructor
@Tag(name = "Items", description = "Items belonging to a Product")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @PostMapping
    @Operation(summary = "Add an item to a product")
    public ResponseEntity<ItemResponse> addItem(@PathVariable Long productId, @Valid @RequestBody ItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.addItemToProduct(productId, request));
    }

    @GetMapping
    @Operation(summary = "List items for a product (paginated)")
    public ResponseEntity<PagedResponse<ItemResponse>> getItems(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(itemService.getItemsByProductId(productId, page, size));
    }
}

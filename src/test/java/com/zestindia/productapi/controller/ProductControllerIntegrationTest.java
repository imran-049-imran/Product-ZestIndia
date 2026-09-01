package com.zestindia.productapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zestindia.productapi.dto.ProductRequest;
import com.zestindia.productapi.entity.AppUser;
import com.zestindia.productapi.repository.AppUserRepository;
import com.zestindia.productapi.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        appUserRepository.deleteAll();
        appUserRepository.save(AppUser.builder()
                .username("tester")
                .password(passwordEncoder.encode("password123"))
                .role(AppUser.Role.ROLE_USER)
                .build());
    }

    @Test
    @WithMockUser(username = "tester")
    void createProduct_shouldReturn201() throws Exception {
        ProductRequest request = new ProductRequest("Wireless Mouse");

        mockMvc.perform(post("/api/v1/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName", is("Wireless Mouse")));
    }

    @Test
    @WithMockUser(username = "tester")
    void createProduct_withBlankName_shouldReturn400() throws Exception {
        ProductRequest request = new ProductRequest("");

        mockMvc.perform(post("/api/v1/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "tester")
    void getProduct_whenNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "tester")
    void getAllProducts_shouldReturnPagedList() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new ProductRequest("Keyboard"))));

        mockMvc.perform(get("/api/v1/products").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    void createProduct_withoutAuth_shouldReturn401() throws Exception {
        ProductRequest request = new ProductRequest("Monitor");

        mockMvc.perform(post("/api/v1/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "tester")
    void deleteProduct_shouldReturn204() throws Exception {
        String response = mockMvc.perform(post("/api/v1/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new ProductRequest("USB Cable"))))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/v1/products/{id}", id))
                .andExpect(status().isNoContent());
    }
}

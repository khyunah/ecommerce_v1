package com.loopers.interfaces.api.like;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class LikeV1ApiE2ETest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        // 테스트에 사용할 상품 생성
        Product product1 = productRepository.save(
                Product.from("상품1", "설명", BigDecimal.valueOf(10000), BigDecimal.valueOf(12000), "ON_SALE", 1L)
        );
        Product product2 = productRepository.save(
                Product.from("상품2", "설명", BigDecimal.valueOf(15000), BigDecimal.valueOf(20000), "ON_SALE", 1L)
        );

        // 좋아요 생성
        likeRepository.save(Like.from(1L, product1.getId()));
        likeRepository.save(Like.from(1L, product2.getId()));
    }

    @Test
    void shouldReturnLikedProductList_whenLikedProductsExist() throws Exception {
        // given
        Long refUserId = 1L;

        // when & then
        mockMvc.perform(get("/api/v1/like/products")
                        .header("X-USER-ID", refUserId.toString())
                        .param("refuserId", "1")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].productName").exists())
                .andExpect(jsonPath("$.content[0].productId").exists())
                .andExpect(jsonPath("$.content[0].liked").value(true))
                .andDo(print());
    }
}

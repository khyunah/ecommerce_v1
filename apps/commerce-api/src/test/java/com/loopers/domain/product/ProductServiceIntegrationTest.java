package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class ProductServiceIntegrationTest {

    private ProductService productSpyService;
    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        ProductService realService = new ProductService(productRepository);
        productSpyService = Mockito.spy(realService);
    }

    @DisplayName("상품 ID가 존재할 경우, 상품 상세가 반환된다.")
    @Test
    void returnProductDetail_whenIdExists() {
        // given
        Product product = Product.from(
                "상품명",
                "상품 설명",
                new BigDecimal("9000"),
                new BigDecimal("10000"),
                "ON_SALE",
                1L
        );
        product = productRepository.save(product);

        // when
        Product foundedProduct = productSpyService.getDetail(product.getId());

        // then
        assertThat(foundedProduct.getName()).isEqualTo("상품명");
        assertThat(foundedProduct.getSellingPrice().getValue().longValue()).isEqualTo(9000L);
        assertThat(foundedProduct.getLikeCount()).isEqualTo(0L);
    }

    @DisplayName("상품 ID가 존재하지 않을 경우, NOT_FOUND 예외가 발생한다.")
    @Test
    void throwNotFoundException_whenProductIdNotFound() {
        // given
        Long nonExistentId = 999L;

        // when
        CoreException exception = assertThrows(CoreException.class, () -> {
            productSpyService.getDetail(nonExistentId);
        });

        // then
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.NOT_FOUND); // ErrorType 확인
        assertThat(exception.getMessage()).isEqualTo("상품 ID가 존재하지 않습니다."); // 메시지 확인
    }

    @DisplayName("상품 ID가 존재할 경우 true를 반환한다.")
    @Test
    void returnTrue_whenProductExists() {
        // given
        Long id = 1L;
        Product product = Product.from("상품", "설명", new BigDecimal("9000"), new BigDecimal("10000"), "ON_SALE", 1L);
        product = productRepository.save(product);

        // when
        boolean exists = productSpyService.existsById(product.getId());

        // then
        assertThat(exists).isTrue();
    }

    @DisplayName("상품 ID가 존재하지 않을 경우 false를 반환한다.")
    @Test
    void returnFalse_whenProductNotExists() {
        // given
        Long id = 100L;

        // when
        boolean exists = productSpyService.existsById(id);

        // then
        assertThat(exists).isFalse();
    }
}

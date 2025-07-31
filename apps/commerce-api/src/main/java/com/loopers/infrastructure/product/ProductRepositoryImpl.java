package com.loopers.infrastructure.product;

import com.loopers.domain.brand.QBrand;
import com.loopers.domain.like.QLike;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.QProduct;
import com.loopers.interfaces.api.product.ProductWithLikeCountDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Product> findById(Long id) {
        return productJpaRepository.findById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return productJpaRepository.existsById(id);
    }

    @Override
    public Product save(Product product) {
        return productJpaRepository.save(product);
    }

    @Override
    public Page<ProductWithLikeCountDto> findProductsWithLikeCount(Long brandId, Pageable pageable) {
        QProduct product = QProduct.product;
        QBrand brand = QBrand.brand;
        QLike like = QLike.like;

        // 좋아요 수 서브쿼리 (상품별 좋아요 수)
        var likeCountSubQuery = queryFactory
                .select(like.refProductId, like.count())
                .from(like)
                .groupBy(like.refProductId);

        // 메인 쿼리
        List<ProductWithLikeCountDto> content = queryFactory
                .select(Projections.constructor(ProductWithLikeCountDto.class,
                        product.id,
                        product.name,
                        product.originalPrice.value,
                        product.sellingPrice.value,
                        product.saleStatus,
                        brand.id,
                        brand.name,
                        // 좋아요 수, 없으면 0으로 처리
                        like.count().coalesce(0L)
                ))
                .from(product)
                .leftJoin(brand).on(product.refBrandId.eq(brand.id))
                .leftJoin(like).on(like.refProductId.eq(product.id))
                .where(brandId != null ? product.refBrandId.eq(brandId) : null)
                .groupBy(product.id, brand.id)
                .orderBy(product.createdAt.desc())  // 최신순 정렬
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 카운트 쿼리 (브랜드 필터 조건 적용)
        long total = queryFactory
                .select(product.count())
                .from(product)
                .where(brandId != null ? product.refBrandId.eq(brandId) : null)
                .fetchOne();

        return PageableExecutionUtils.getPage(content, pageable, () -> total);
    }
}

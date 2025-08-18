package com.loopers.infrastructure.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.like.QLike;
import com.loopers.domain.like.dto.LikedProductDto;
import com.loopers.domain.product.QProduct;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;

@RequiredArgsConstructor
@Component
public class LikeRepositoryImpl implements LikeRepository {

    private final LikeJpaRepository likeJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Like save(Like like) {
        return likeJpaRepository.save(like);
    }

    @Override
    public boolean existsByRefUserIdAndRefProductId(Long refUserId, Long refProductId) {
        return likeJpaRepository.existsByRefUserIdAndRefProductId(refUserId, refProductId);
    }

    @Override
    public int delete(Long refUserId, Long refProductId) {
        return likeJpaRepository.deleteByRefUserIdAndRefProductId(refUserId, refProductId);
    }

    @Override
    public Page<LikedProductDto> findLikedProductsByRefUserId(Long refUserId, Pageable pageable) {
        QLike like = QLike.like;
        QProduct product = QProduct.product;

        // 서브쿼리로 상품별 좋아요 수 집계
        JPQLQuery<Long> likeCountSubQuery = select(like.count())
                .from(like)
                .where(like.refProductId.eq(product.id));

        List<LikedProductDto> content = queryFactory
                .select(Projections.constructor(LikedProductDto.class,
                        like.id,                        // 좋아요 ID
                        product.id,                    // 상품 ID
                        product.name,                  // 상품명
                        product.originalPrice.value,  // 원가
                        product.sellingPrice.value,   // 할인가
                        product.saleStatus.stringValue(), // 판매상태 (enum string)
                        Expressions.asBoolean(true),  // 좋아요 여부 (조회하는 userId 기준이니까 항상 true)
                        likeCountSubQuery              // 좋아요 수 (서브쿼리)
                ))
                .from(like)
                .join(product).on(like.refProductId.eq(product.id))
                .where(like.refUserId.eq(refUserId))
                .orderBy(like.createdAt.desc()) // 최신순 정렬 (BaseEntity에 createdAt 필드 있다고 가정)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 카운트 쿼리
        long total = queryFactory
                .select(like.count())
                .from(like)
                .where(like.refUserId.eq(refUserId))
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public long countLikeByProductId(Long productId) {
        QLike like = QLike.like;
        return queryFactory
                .select(like.count())
                .from(like)
                .where(like.refProductId.eq(productId))
                .fetchOne();
    }

    @Override
    public List<Like> findAllByProductId(Long productId1) {
        return likeJpaRepository.findAllByRefProductId(productId1);
    }

    @Override
    public List<Like> findAllByUserIdAndProductId(Long userId1, Long productId1) {
        return likeJpaRepository.findAllByRefUserIdAndRefProductId(userId1, productId1);
    }

    @Override
    public void deleteAll() {
        likeJpaRepository.deleteAll();
    }

}

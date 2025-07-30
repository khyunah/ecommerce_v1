package com.loopers.domain.like;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "like")
public class Like extends BaseEntity {

    @Column(nullable = false)
    private Long refUserId;
    @Column(nullable = false)
    private Long refProductId;

    public static Like from(Long refUserId, Long refProductId) {
        validateRefUserId(refUserId);
        validateRefProductId(refProductId);
        return new Like(
                refUserId,
                refProductId
        );
    }

    public static void validateRefUserId(Long refUserId) {
        if (refUserId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "참조 사용자 ID는 null일 수 없습니다.");
        } else if(refUserId < 0){
            throw new CoreException(ErrorType.BAD_REQUEST, "참조 사용자 ID는 음수일 수 없습니다.");
        }
    }

    public static void validateRefProductId(Long refProductId) {
        if (refProductId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "참조 상품 ID는 null일 수 없습니다.");
        } else if(refProductId < 0){
            throw new CoreException(ErrorType.BAD_REQUEST, "참조 상품 ID는 음수일 수 없습니다.");
        }
    }

}

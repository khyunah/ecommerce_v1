package com.loopers.domain.brand;

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
@Getter
@Table(name = "brand")
@NoArgsConstructor
@AllArgsConstructor
public class Brand extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String description;

    public static Brand from(String name, String description){
        validate(name);
        return new Brand(name,description);
    }

    public static void validate(String name){
        if (name == null || name.isBlank()){
            throw new CoreException(ErrorType.BAD_REQUEST, "브랜드명은 null이거나 빈 문자열일 수 없습니다.");
        }
    }
}

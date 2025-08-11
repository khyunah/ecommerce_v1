package com.loopers.application.dummy;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.like.Like;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.vo.Money;
import com.loopers.domain.product.vo.SaleStatus;
import com.loopers.domain.user.vo.BirthDate;
import com.loopers.domain.user.User;
import com.loopers.domain.user.vo.Email;
import com.loopers.domain.user.vo.Gender;
import com.loopers.domain.user.vo.UserId;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.instancio.Instancio;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import static org.instancio.Select.field;


@Service
@RequiredArgsConstructor
public class DummyDataInsertService {

    private final EntityManagerFactory emf;

    public void bulkInsertBrands(int count) {
        SessionFactory sessionFactory = emf.unwrap(SessionFactory.class);
        StatelessSession session = sessionFactory.openStatelessSession();
        Transaction tx = session.beginTransaction();

        for (int i = 0; i < count; i++) {
            int finalI = i;
            Brand brand = Instancio.of(Brand.class)
                    .supply(field("name"), () -> "브랜드-" + finalI)
                    .supply(field("description"), () -> "브랜드설명-" + finalI)
                    // BaseEntity 필드들을 BaseEntity 클래스를 명시해서 설정
                    .set(field(BaseEntity.class, "createdAt"), LocalDateTime.now())
                    .set(field(BaseEntity.class, "updatedAt"), LocalDateTime.now())
                    .set(field(BaseEntity.class, "deletedAt"), (LocalDateTime) null)
                    .create();

            session.insert(brand);

            if (i % 1000 == 0) {
                System.out.println("Inserted Brand: " + i);
            }
        }

        tx.commit();
        session.close();
    }

    public void bulkInsertProducts(int count) {
        SessionFactory sessionFactory = emf.unwrap(SessionFactory.class);
        StatelessSession session = sessionFactory.openStatelessSession();
        Transaction tx = session.beginTransaction();

        for (int i = 0; i < count; i++) {
            int finalI = i;
            
            // 3년 이내의 랜덤 createdAt 생성
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime threeYearsAgo = now.minusYears(3);
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(threeYearsAgo, now);
            long randomDays = ThreadLocalRandom.current().nextLong(0, daysBetween + 1);
            LocalDateTime randomCreatedAt = threeYearsAgo.plusDays(randomDays);
            
            Product product = Instancio.of(Product.class)
                    .supply(field("name"), () -> "상품-" + finalI)
                    .supply(field("description"), () -> "상품설명-" + finalI)
                    .supply(field("originalPrice"), () -> Money.from(BigDecimal.valueOf(ThreadLocalRandom.current().nextLong(25000, 30000))))
                    .supply(field("sellingPrice"), () -> Money.from(BigDecimal.valueOf(ThreadLocalRandom.current().nextLong(10000, 24000))))
                    .supply(field("saleStatus"), () -> SaleStatus.ON_SALE)
                    .supply(field("refBrandId"), () -> ThreadLocalRandom.current().nextLong(1, 20))
                    // BaseEntity 필드들 - createdAt은 랜덤, updatedAt은 createdAt 이후
                    .set(field(BaseEntity.class, "createdAt"), randomCreatedAt)
                    .set(field(BaseEntity.class, "updatedAt"), randomCreatedAt.plusDays(ThreadLocalRandom.current().nextLong(0, 30)))
                    .set(field(BaseEntity.class, "deletedAt"), (LocalDateTime) null)
                    .create();

            session.insert(product);

            if (i % 1000 == 0) {
                System.out.println("Inserted Product: " + i);
            }
        }

        tx.commit();
        session.close();
    }

    public void bulkInsertLikes(int count) {
        SessionFactory sessionFactory = emf.unwrap(SessionFactory.class);
        StatelessSession session = sessionFactory.openStatelessSession();
        Transaction tx = session.beginTransaction();

        for (int i = 0; i < count; i++) {
            int finalI = i;
            Like like = Instancio.of(Like.class)
                    .supply(field("refUserId"), () -> ThreadLocalRandom.current().nextLong(1, 100))
                    .supply(field("refProductId"), () -> ThreadLocalRandom.current().nextLong(1, 100000))
                    // BaseEntity 필드들을 BaseEntity 클래스를 명시해서 설정
                    .set(field(BaseEntity.class, "createdAt"), LocalDateTime.now())
                    .set(field(BaseEntity.class, "updatedAt"), LocalDateTime.now())
                    .set(field(BaseEntity.class, "deletedAt"), (LocalDateTime) null)
                    .create();

            session.insert(like);

            if (i % 1000 == 0) {
                System.out.println("Inserted Like: " + i);
            }
        }

        tx.commit();
        session.close();
    }

    public void bulkInsertUsers(int count) {
        SessionFactory sessionFactory = emf.unwrap(SessionFactory.class);
        StatelessSession session = sessionFactory.openStatelessSession();
        Transaction tx = session.beginTransaction();

        for (int i = 0; i < count; i++) {
            int finalI = i;
            
            try {
                // 현재 시간을 미리 생성
                LocalDateTime now = LocalDateTime.now();
                
                User user = Instancio.of(User.class)
                        .supply(field("userId"), () -> UserId.from("user" + finalI))
                        .supply(field("email"), () -> Email.from("user" + finalI + "@google.com"))
                        .supply(field("birthDate"), () -> {
                            // 랜덤 생년월일 생성 (1970~2005년 사이)
                            int year = ThreadLocalRandom.current().nextInt(1970, 2006);
                            int month = ThreadLocalRandom.current().nextInt(1, 13);
                            int day = ThreadLocalRandom.current().nextInt(1, 29);
                            String dateString = String.format("%04d-%02d-%02d", year, month, day);
                            return BirthDate.from(dateString);
                        })
                        .supply(field("gender"), () -> Gender.from((finalI % 2 == 0) ? "M" : "F"))
                        // BaseEntity 필드들을 BaseEntity 클래스를 명시해서 설정
                        .set(field(BaseEntity.class, "createdAt"), now)
                        .set(field(BaseEntity.class, "updatedAt"), now)
                        .set(field(BaseEntity.class, "deletedAt"), (LocalDateTime) null)
                        .create();
                
                if (user != null) {
                    session.insert(user);
                } else {
                    System.err.println("User is null for index: " + finalI);
                }
                
            } catch (Exception e) {
                System.err.println("Error creating user " + finalI + ": " + e.getMessage());
                e.printStackTrace();
                // 계속 진행
            }

            if (i % 1000 == 0) {
                System.out.println("Inserted User: " + i);
            }
        }

        tx.commit();
        session.close();
    }
}

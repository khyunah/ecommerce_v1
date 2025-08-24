package com.loopers.domain.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentMethodTest {

    @DisplayName("CARD 결제 방법은 PG사 연결이 필요하다")
    @Test
    void card_payment_requires_pg_connection() {
        // given
        PaymentMethod paymentMethod = PaymentMethod.CARD;

        // when
        boolean requiresPg = paymentMethod.requiresPgConnection();

        // then
        assertThat(requiresPg).isTrue();
    }

    @DisplayName("CARD가 아닌 결제 방법들은 PG사 연결이 불필요하다")
    @ParameterizedTest
    @EnumSource(value = PaymentMethod.class, names = {"POINT_ONLY"})
    void non_card_payment_methods_do_not_require_pg_connection(PaymentMethod paymentMethod) {
        // when
        boolean requiresPg = paymentMethod.requiresPgConnection();

        // then
        assertThat(requiresPg).isFalse();
    }

    @DisplayName("PaymentMethod.from() 메서드로 문자열을 올바르게 변환한다")
    @Test
    void from_method_converts_string_to_payment_method() {
        // when & then
        assertThat(PaymentMethod.from("CARD")).isEqualTo(PaymentMethod.CARD);
        assertThat(PaymentMethod.from("POINT_ONLY")).isEqualTo(PaymentMethod.POINT_ONLY);
    }
}

package brucehan.order.domain;

public enum OrderStatus {
    /*
    결제대기 / 승인중 / 승인결과불명 / 승인완료 / 주문완료 / 결제실패 / 주문 실패
     */
    AWAITING,
    PENDING,
    UNKNOWN,
    APPROVED,
    COMPLETE,
    PAYMENT_FAILED,
    ORDER_FAILED
}

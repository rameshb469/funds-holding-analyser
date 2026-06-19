package com.rms.funds.holdings.analyser.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "kite_order_audit")
public class KiteOrderAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_id")
    private Long stockId;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "exchange")
    private String exchange;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "order_type")
    private String orderType;

    @Column(name = "product")
    private String product;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price")
    private Double price;

    @Column(name = "trigger_price")
    private Double triggerPrice;

    @Column(name = "tag")
    private String tag;

    @Column(name = "validity")
    private String validity;

    @Column(name = "kite_order_id")
    private String kiteOrderId;

    @Column(name = "status", length = 2000)
    private String status;

    @Column(name = "request_json", length = 4000)
    private String requestJson;

    @Column(name = "response_json", length = 4000)
    private String responseJson;

    @Column(name = "sandbox")
    private boolean sandbox;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

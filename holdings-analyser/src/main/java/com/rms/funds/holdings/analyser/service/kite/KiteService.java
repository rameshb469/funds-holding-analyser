package com.rms.funds.holdings.analyser.service.kite;

import com.rms.funds.holdings.analyser.controller.dto.KiteLoginUrlDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteOrderAuditDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteOrderDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteOrderRequestDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteOrderResponseDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteSessionStatusDto;

import java.util.List;

public interface KiteService {

    KiteLoginUrlDto getLoginUrl();

    String handleCallback(String requestToken);

    KiteSessionStatusDto getSessionStatus();

    KiteOrderResponseDto placeOrder(KiteOrderRequestDto request);

    List<KiteOrderDto> getOrderBook();

    List<KiteOrderAuditDto> getRecentAudits();

    /**
     * Real-time quote (LTP, OHLC, depth) for one instrument. Requires a live
     * Kite session. The returned envelope carries {@code paidDataRequired=true}
     * when Kite rejects with a data/permission error (e.g. free plan).
     */
    KiteModels.KiteQuoteEnvelope getQuote(String exchange, String tradingSymbol);

    /**
     * Batch quote fetch for up to N instruments in one Kite call. Each input must
     * be in the form {@code "EXCHANGE:SYMBOL"}; the returned envelopes are
     * positionally aligned with the input list.
     */
    List<KiteModels.KiteQuoteEnvelope> getQuotes(List<String> exchangeSymbols);

    /**
     * Cancel a regular Kite order. Used by the order-execution agent to ratchet
     * the SL-M up the ladder.
     */
    void cancelOrder(String kiteOrderId);
}

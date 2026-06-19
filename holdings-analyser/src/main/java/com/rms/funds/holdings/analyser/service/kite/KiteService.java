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
}

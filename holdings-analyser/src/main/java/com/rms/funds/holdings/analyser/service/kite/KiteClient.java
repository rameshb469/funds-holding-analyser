package com.rms.funds.holdings.analyser.service.kite;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rms.funds.holdings.analyser.config.KiteConfigProperties;
import com.rms.funds.holdings.analyser.controller.dto.KiteOrderRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class KiteClient {

    private final RestTemplate restTemplate;
    private final KiteConfigProperties props;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String buildLoginUrl() {
        String url = "https://kite.zerodha.com/connect/login"
                + "?api_key=" + urlEncode(props.getApiKey())
                + "&v=3";
        return url;
    }

    public KiteModels.KiteTokenResponse exchangeRequestToken(String requestToken) {
        String checksum = sha256(props.getApiKey() + requestToken + props.getApiSecret());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("api_key", props.getApiKey());
        form.add("request_token", requestToken);
        form.add("checksum", checksum);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

        ResponseEntity<JsonNode> resp = restTemplate.exchange(
                props.getBaseUrl() + "/session/token",
                HttpMethod.POST,
                entity,
                JsonNode.class
        );

        JsonNode data = resp.getBody() != null ? resp.getBody().get("data") : null;
        if (data == null) {
            throw new KiteException(500, "error", "TokenExchange",
                    "Kite response missing 'data' field: " + resp.getBody());
        }
        return KiteModels.KiteTokenResponse.builder()
                .accessToken(textOrNull(data, "access_token"))
                .userId(textOrNull(data, "user_id"))
                .userName(textOrNull(data, "user_name"))
                .loginTime(textOrNull(data, "login_time"))
                .publicToken(textOrNull(data, "public_token"))
                .apiKey(textOrNull(data, "api_key"))
                .build();
    }

    public KiteModels.KiteOrderResponse placeOrder(KiteOrderRequestDto req, String accessToken) {
        HttpHeaders headers = authHeaders(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<KiteOrderRequestDto> entity = new HttpEntity<>(req, headers);

        ResponseEntity<JsonNode> resp = invokeWithErrorHandling(
                props.getBaseUrl() + "/orders/regular",
                HttpMethod.POST,
                entity,
                JsonNode.class
        );

        JsonNode data = resp.getBody() != null ? resp.getBody().get("data") : null;
        String orderId = data != null ? textOrNull(data, "order_id") : null;
        return KiteModels.KiteOrderResponse.builder()
                .orderId(orderId)
                .raw(resp.getBody() != null ? resp.getBody().toString() : null)
                .build();
    }

    public List<KiteModels.KiteOrderEntry> getOrderBook(String accessToken) {
        HttpHeaders headers = authHeaders(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> resp = invokeWithErrorHandling(
                props.getBaseUrl() + "/orders",
                HttpMethod.GET,
                entity,
                JsonNode.class
        );

        if (resp.getBody() == null || resp.getBody().get("data") == null) {
            return Collections.emptyList();
        }
        return parseList(resp.getBody().get("data"), KiteModels.KiteOrderEntry.class);
    }

    public KiteModels.KiteQuoteEnvelope getQuote(String exchange, String tradingsymbol, String accessToken) {
        String instrument = exchange + ":" + tradingsymbol;
        HttpHeaders headers = authHeaders(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<JsonNode> resp = invokeWithErrorHandling(
                    props.getBaseUrl() + "/quote?i=" + urlEncode(instrument),
                    HttpMethod.GET,
                    entity,
                    JsonNode.class
            );
            return parseQuoteEnvelope(resp, instrument);
        } catch (KiteException ex) {
            if (isPaidDataRequired(ex.getErrorType())) {
                return KiteModels.KiteQuoteEnvelope.builder()
                        .paidDataRequired(true)
                        .errorType(ex.getErrorType())
                        .message(ex.getMessage())
                        .build();
            }
            throw ex;
        }
    }

    /**
     * Multi-instrument quote fetch. Kite accepts repeated {@code i=} query params and
     * the response is keyed by the same "EXCHANGE:SYMBOL" identifiers. Returns a list of
     * (instrumentKey, envelope) pairs in the order the caller asked for. On paid-data
     * errors the envelope carries {@code paidDataRequired=true} and a null quote.
     */
    public List<KiteModels.KiteQuoteEnvelope> getQuotes(List<String> exchangeSymbols, String accessToken) {
        if (exchangeSymbols == null || exchangeSymbols.isEmpty()) {
            return Collections.emptyList();
        }
        StringBuilder url = new StringBuilder(props.getBaseUrl()).append("/quote?");
        for (int i = 0; i < exchangeSymbols.size(); i++) {
            if (i > 0) {
                url.append("&");
            }
            url.append("i=").append(urlEncode(exchangeSymbols.get(i)));
        }
        HttpHeaders headers = authHeaders(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<JsonNode> resp = invokeWithErrorHandling(
                    url.toString(), HttpMethod.GET, entity, JsonNode.class);
            JsonNode body = resp.getBody();
            java.util.List<KiteModels.KiteQuoteEnvelope> out = new java.util.ArrayList<>(exchangeSymbols.size());
            if (body == null) {
                return out;
            }
            for (String key : exchangeSymbols) {
                out.add(parseQuoteEnvelopeFromBody(body, key));
            }
            return out;
        } catch (KiteException ex) {
            if (isPaidDataRequired(ex.getErrorType())) {
                java.util.List<KiteModels.KiteQuoteEnvelope> out = new java.util.ArrayList<>(exchangeSymbols.size());
                for (int i = 0; i < exchangeSymbols.size(); i++) {
                    out.add(KiteModels.KiteQuoteEnvelope.builder()
                            .paidDataRequired(true)
                            .errorType(ex.getErrorType())
                            .message(ex.getMessage())
                            .build());
                }
                return out;
            }
            throw ex;
        }
    }

    public void cancelOrder(String kiteOrderId, String accessToken) {
        HttpHeaders headers = authHeaders(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        invokeWithErrorHandling(
                props.getBaseUrl() + "/orders/regular/" + urlEncode(kiteOrderId),
                HttpMethod.DELETE,
                entity,
                JsonNode.class
        );
    }

    public KiteModels.KiteQuoteEnvelope getQuoteOhlc(String exchange, String tradingsymbol, String accessToken) {
        String instrument = exchange + ":" + tradingsymbol;
        HttpHeaders headers = authHeaders(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<JsonNode> resp = invokeWithErrorHandling(
                    props.getBaseUrl() + "/quote/ohlc?i=" + urlEncode(instrument),
                    HttpMethod.GET,
                    entity,
                    JsonNode.class
            );
            return parseQuoteEnvelope(resp, instrument);
        } catch (KiteException ex) {
            if (isPaidDataRequired(ex.getErrorType())) {
                return KiteModels.KiteQuoteEnvelope.builder()
                        .paidDataRequired(true)
                        .errorType(ex.getErrorType())
                        .message(ex.getMessage())
                        .build();
            }
            throw ex;
        }
    }

    private KiteModels.KiteQuoteEnvelope parseQuoteEnvelope(ResponseEntity<JsonNode> resp, String instrument) {
        if (resp == null || resp.getBody() == null) {
            return KiteModels.KiteQuoteEnvelope.builder()
                    .paidDataRequired(false)
                    .build();
        }
        return parseQuoteEnvelopeFromBody(resp.getBody(), instrument);
    }

    private KiteModels.KiteQuoteEnvelope parseQuoteEnvelopeFromBody(JsonNode body, String instrument) {
        if (body == null) {
            return KiteModels.KiteQuoteEnvelope.builder()
                    .paidDataRequired(false)
                    .build();
        }
        JsonNode data = body.get("data");
        JsonNode instrumentNode = data != null ? data.get(instrument) : null;
        if (instrumentNode == null && data != null && data.get("ohlc") != null) {
            instrumentNode = data.get("ohlc").get(instrument);
        }
        KiteModels.KiteQuote quote = null;
        if (instrumentNode != null && instrumentNode.isObject()) {
            try {
                quote = objectMapper.treeToValue(instrumentNode, KiteModels.KiteQuote.class);
            } catch (IOException e) {
                log.debug("Could not parse Kite quote body for {}: {}", instrument, e.getMessage());
            }
        }
        return KiteModels.KiteQuoteEnvelope.builder()
                .raw(body.toString())
                .quote(quote)
                .paidDataRequired(false)
                .build();
    }

    private static boolean isPaidDataRequired(String errorType) {
        if (errorType == null) {
            return false;
        }
        String t = errorType.toLowerCase();
        return t.contains("data")
                || t.contains("permission")
                || t.contains("denied")
                || t.contains("subscription")
                || t.contains("forbidden");
    }

    public List<KiteModels.KitePosition> getPositions(String accessToken) {
        HttpHeaders headers = authHeaders(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> resp = invokeWithErrorHandling(
                props.getBaseUrl() + "/portfolio/positions",
                HttpMethod.GET,
                entity,
                JsonNode.class
        );

        if (resp.getBody() == null) {
            return Collections.emptyList();
        }
        JsonNode day = resp.getBody().get("data") != null ? resp.getBody().get("data").get("day") : null;
        JsonNode net = resp.getBody().get("data") != null ? resp.getBody().get("data").get("net") : null;
        java.util.Map<String, KiteModels.KitePosition> byKey = new java.util.LinkedHashMap<>();
        mergePositions(byKey, day);
        mergePositions(byKey, net);
        return new java.util.ArrayList<>(byKey.values());
    }

    private void mergePositions(java.util.Map<String, KiteModels.KitePosition> sink, JsonNode arr) {
        if (arr == null || !arr.isArray()) {
            return;
        }
        for (JsonNode node : arr) {
            if (node == null) {
                continue;
            }
            try {
                KiteModels.KitePosition p = objectMapper.treeToValue(node, KiteModels.KitePosition.class);
                String key = (p.getExchange() == null ? "" : p.getExchange()) + "|" + (p.getTradingSymbol() == null ? "" : p.getTradingSymbol());
                sink.merge(key, p, (a, b) -> a);
            } catch (IOException e) {
                log.debug("Skipping unparseable position row: {}", node);
            }
        }
    }

    private <T> List<T> parseList(JsonNode data, Class<T> elementType) {
        try {
            return objectMapper.readValue(
                    data.traverse(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, elementType)
            );
        } catch (IOException e) {
            log.warn("Failed to parse Kite response into {}", elementType.getSimpleName(), e);
            return Collections.emptyList();
        }
    }

    private HttpHeaders authHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + props.getApiKey() + ":" + accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X-Kite-Version", "3");
        return headers;
    }

    private <T> ResponseEntity<T> invokeWithErrorHandling(String url, HttpMethod method,
                                                          HttpEntity<?> entity, Class<T> responseType) {
        try {
            return restTemplate.exchange(url, method, entity, responseType);
        } catch (HttpStatusCodeException ex) {
            int code = ex.getStatusCode().value();
            String body = ex.getResponseBodyAsString(StandardCharsets.UTF_8);
            String kiteStatus = null;
            String errorType = null;
            String message = body;
            try {
                JsonNode node = objectMapper.readTree(body);
                kiteStatus = textOrNull(node, "status");
                errorType = textOrNull(node, "error_type");
                JsonNode msg = node.get("message");
                if (msg != null) {
                    message = msg.isTextual() ? msg.asText() : msg.toString();
                }
            } catch (Exception parseFailure) {
                log.debug("Could not parse Kite error body: {}", body);
            }
            throw new KiteException(code, kiteStatus, errorType, message);
        }
    }

    private static String textOrNull(JsonNode parent, String field) {
        if (parent == null) {
            return null;
        }
        JsonNode node = parent.get(field);
        return node == null || node.isNull() ? null : node.asText();
    }

    private static String urlEncode(String s) {
        return java.net.URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}

package com.sch.energyBrokerService.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.sch.energyBrokerService.adapter.in.web.dto.Request.PriceQuoteRequest;
import com.sch.energyBrokerService.adapter.in.web.dto.Response.TariffQuoteResponse;
import com.sch.energyBrokerService.application.service.pricing.query.PriceQuoteQueryHandler;

class PricingControllerTest {

    private final PriceQuoteQueryHandler quoteHandler = mock(PriceQuoteQueryHandler.class);
    private final PricingController controller = new PricingController(quoteHandler);

    @Test
    void forwardsTheQuoteRequestAndReturnsTheHandlerResults() {
        BigDecimal kwh = new BigDecimal("12.5");
        TariffQuoteResponse quote = new TariffQuoteResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "FLAT",
                new BigDecimal("0.2500"),
                kwh,
                new BigDecimal("3.13"));
        when(quoteHandler.quote("Charge Spa", kwh)).thenReturn(List.of(quote));

        List<TariffQuoteResponse> result = controller.quote(new PriceQuoteRequest("Charge Spa", kwh));

        assertThat(result).containsExactly(quote);
        verify(quoteHandler).quote("Charge Spa", kwh);
    }
}

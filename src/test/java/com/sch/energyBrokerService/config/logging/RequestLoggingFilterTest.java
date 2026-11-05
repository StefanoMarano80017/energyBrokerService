package com.sch.energyBrokerService.config.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    @Test
    void reusesTheRequestIdHeaderAndClearsItFromTheMdc() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Request-Id", "request-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            assertThat(MDC.get("requestId")).isEqualTo("request-123");
        });

        assertThat(response.getHeader("X-Request-Id")).isEqualTo("request-123");
        assertThat(MDC.get("requestId")).isNull();
    }

    @Test
    void generatesARequestIdWhenTheHeaderIsBlank() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Request-Id", "  ");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            assertThat(MDC.get("requestId")).isNotBlank();
        });

        assertThat(response.getHeader("X-Request-Id")).isNotBlank();
        assertThat(MDC.get("requestId")).isNull();
    }
}

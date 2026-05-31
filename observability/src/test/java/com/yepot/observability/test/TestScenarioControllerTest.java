package com.yepot.observability.test;

import static org.hamcrest.Matchers.containsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yepot.observability.account.domain.Account;
import com.yepot.observability.account.repository.AccountRepository;
import com.yepot.observability.account.repository.AccountTransactionRepository;
import com.yepot.observability.test.dto.request.MassTransferRequest;
import com.yepot.observability.test.metric.TestScenarioMetrics;
import com.yepot.observability.transfer.repository.TransferTransactionRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class TestScenarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountTransactionRepository accountTransactionRepository;

    @Autowired
    private TransferTransactionRepository transferTransactionRepository;

    @Autowired
    private MeterRegistry meterRegistry;

    @AfterEach
    void tearDown() {
        transferTransactionRepository.deleteAll();
        accountTransactionRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    void slowTransferReturnsDelayedResponseAndRecordsMetrics() throws Exception {
        double successCounterBefore = counterCount(
            TestScenarioMetrics.SCENARIO_REQUESTS_METRIC,
            "scenario",
            "slow-transfer",
            "result",
            "success"
        );
        long durationCountBefore = timerCount(
            TestScenarioMetrics.SCENARIO_DURATION_METRIC,
            "scenario",
            "slow-transfer",
            "result",
            "success"
        );
        double durationMsBefore = timerTotalMs(
            TestScenarioMetrics.SCENARIO_DURATION_METRIC,
            "scenario",
            "slow-transfer",
            "result",
            "success"
        );

        mockMvc.perform(get("/test/slow-transfer"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("slow transfer response"))
            .andExpect(jsonPath("$.delayMs").value(3000));

        assertThat(counterCount(
            TestScenarioMetrics.SCENARIO_REQUESTS_METRIC,
            "scenario",
            "slow-transfer",
            "result",
            "success"
        )).isEqualTo(successCounterBefore + 1.0);
        assertThat(timerCount(
            TestScenarioMetrics.SCENARIO_DURATION_METRIC,
            "scenario",
            "slow-transfer",
            "result",
            "success"
        )).isEqualTo(durationCountBefore + 1L);
        assertThat(timerTotalMs(
            TestScenarioMetrics.SCENARIO_DURATION_METRIC,
            "scenario",
            "slow-transfer",
            "result",
            "success"
        ) - durationMsBefore).isGreaterThanOrEqualTo(2900.0);
    }

    @Test
    void errorTransferReturnsInternalServerErrorAndRecordsFailureMetrics() throws Exception {
        double failureCounterBefore = counterCount(
            TestScenarioMetrics.SCENARIO_REQUESTS_METRIC,
            "scenario",
            "error-transfer",
            "result",
            "failure"
        );
        long durationCountBefore = timerCount(
            TestScenarioMetrics.SCENARIO_DURATION_METRIC,
            "scenario",
            "error-transfer",
            "result",
            "failure"
        );

        mockMvc.perform(get("/test/error-transfer"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.timestamp").isString())
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").value("Internal Server Error"))
            .andExpect(jsonPath("$.message").value("의도적으로 발생시킨 에러입니다."))
            .andExpect(jsonPath("$.path").value("/test/error-transfer"));

        assertThat(counterCount(
            TestScenarioMetrics.SCENARIO_REQUESTS_METRIC,
            "scenario",
            "error-transfer",
            "result",
            "failure"
        )).isEqualTo(failureCounterBefore + 1.0);
        assertThat(timerCount(
            TestScenarioMetrics.SCENARIO_DURATION_METRIC,
            "scenario",
            "error-transfer",
            "result",
            "failure"
        )).isEqualTo(durationCountBefore + 1L);
    }

    @Test
    void massTransferReturnsAggregatedResultAndRecordsBusinessMetrics() throws Exception {
        Account fromAccount = saveAccount("양은서", 95000L, "100-0000-0001");
        Account toAccount = saveAccount("김예봄", 0L, "100-0000-0002");
        MassTransferRequest request = new MassTransferRequest(
            fromAccount.getAccountId(),
            toAccount.getAccountId(),
            1000L,
            100
        );
        double scenarioSuccessCounterBefore = counterCount(
            TestScenarioMetrics.SCENARIO_REQUESTS_METRIC,
            "scenario",
            "mass-transfer",
            "result",
            "success"
        );
        double successAttemptsBefore = counterCount(
            TestScenarioMetrics.MASS_TRANSFER_ATTEMPTS_METRIC,
            "result",
            "success"
        );
        double failureAttemptsBefore = counterCount(
            TestScenarioMetrics.MASS_TRANSFER_ATTEMPTS_METRIC,
            "result",
            "failure"
        );
        long requestSizeCountBefore = summaryCount(TestScenarioMetrics.MASS_TRANSFER_REQUEST_SIZE_METRIC);
        double requestSizeTotalBefore = summaryTotalAmount(TestScenarioMetrics.MASS_TRANSFER_REQUEST_SIZE_METRIC);
        double successSizeTotalBefore = summaryTotalAmount(TestScenarioMetrics.MASS_TRANSFER_SUCCESS_SIZE_METRIC);
        double failureSizeTotalBefore = summaryTotalAmount(TestScenarioMetrics.MASS_TRANSFER_FAILURE_SIZE_METRIC);
        double amountTotalBefore = summaryTotalAmount(TestScenarioMetrics.MASS_TRANSFER_AMOUNT_METRIC);

        mockMvc.perform(post("/test/mass-transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.requestedCount").value(100))
            .andExpect(jsonPath("$.successCount").value(95))
            .andExpect(jsonPath("$.failCount").value(5))
            .andExpect(jsonPath("$.totalAmount").value(95000))
            .andExpect(jsonPath("$.durationMs").isNumber());

        assertThat(counterCount(
            TestScenarioMetrics.SCENARIO_REQUESTS_METRIC,
            "scenario",
            "mass-transfer",
            "result",
            "success"
        )).isEqualTo(scenarioSuccessCounterBefore + 1.0);
        assertThat(counterCount(
            TestScenarioMetrics.MASS_TRANSFER_ATTEMPTS_METRIC,
            "result",
            "success"
        )).isEqualTo(successAttemptsBefore + 95.0);
        assertThat(counterCount(
            TestScenarioMetrics.MASS_TRANSFER_ATTEMPTS_METRIC,
            "result",
            "failure"
        )).isEqualTo(failureAttemptsBefore + 5.0);
        assertThat(summaryCount(TestScenarioMetrics.MASS_TRANSFER_REQUEST_SIZE_METRIC))
            .isEqualTo(requestSizeCountBefore + 1L);
        assertThat(summaryTotalAmount(TestScenarioMetrics.MASS_TRANSFER_REQUEST_SIZE_METRIC))
            .isEqualTo(requestSizeTotalBefore + 100.0);
        assertThat(summaryTotalAmount(TestScenarioMetrics.MASS_TRANSFER_SUCCESS_SIZE_METRIC))
            .isEqualTo(successSizeTotalBefore + 95.0);
        assertThat(summaryTotalAmount(TestScenarioMetrics.MASS_TRANSFER_FAILURE_SIZE_METRIC))
            .isEqualTo(failureSizeTotalBefore + 5.0);
        assertThat(summaryTotalAmount(TestScenarioMetrics.MASS_TRANSFER_AMOUNT_METRIC))
            .isEqualTo(amountTotalBefore + 95000.0);
    }

    @Test
    void massTransferWithInvalidCountReturnsBadRequest() throws Exception {
        MassTransferRequest request = new MassTransferRequest(1L, 2L, 1000L, 0);

        mockMvc.perform(post("/test/mass-transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.timestamp").isString())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Illegal Argument"))
            .andExpect(jsonPath("$.message").value("반복 횟수는 1 이상이어야 합니다."))
            .andExpect(jsonPath("$.path").value("/test/mass-transfer"));
    }

    @Test
    void prometheusEndpointExposesCustomScenarioMetrics() throws Exception {
        Account fromAccount = saveAccount("양은서", 95000L, "100-0000-0001");
        Account toAccount = saveAccount("김예봄", 0L, "100-0000-0002");
        MassTransferRequest request = new MassTransferRequest(
            fromAccount.getAccountId(),
            toAccount.getAccountId(),
            1000L,
            100
        );

        mockMvc.perform(get("/test/slow-transfer"))
            .andExpect(status().isOk());
        mockMvc.perform(get("/test/error-transfer"))
            .andExpect(status().isInternalServerError());
        mockMvc.perform(post("/test/mass-transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/prometheus"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("observability_test_scenario_requests_total")))
            .andExpect(content().string(containsString("observability_test_scenario_duration_seconds_count")))
            .andExpect(content().string(containsString("observability_test_mass_transfer_attempts_total")))
            .andExpect(content().string(containsString("observability_test_mass_transfer_request_size_count")))
            .andExpect(content().string(containsString("observability_test_mass_transfer_amount_sum")));
    }

    private Account saveAccount(String userName, Long balance, String accountNumber) {
        Account account = accountRepository.save(new Account(userName, balance));
        account.updateAccountNumber(accountNumber);
        return accountRepository.save(account);
    }

    private double counterCount(String name, String... tags) {
        Counter counter = meterRegistry.find(name).tags(tags).counter();
        return counter == null ? 0.0 : counter.count();
    }

    private long timerCount(String name, String... tags) {
        Timer timer = meterRegistry.find(name).tags(tags).timer();
        return timer == null ? 0L : timer.count();
    }

    private double timerTotalMs(String name, String... tags) {
        Timer timer = meterRegistry.find(name).tags(tags).timer();
        return timer == null ? 0.0 : timer.totalTime(TimeUnit.MILLISECONDS);
    }

    private long summaryCount(String name) {
        DistributionSummary summary = meterRegistry.find(name).summary();
        return summary == null ? 0L : summary.count();
    }

    private double summaryTotalAmount(String name) {
        DistributionSummary summary = meterRegistry.find(name).summary();
        return summary == null ? 0.0 : summary.totalAmount();
    }
}

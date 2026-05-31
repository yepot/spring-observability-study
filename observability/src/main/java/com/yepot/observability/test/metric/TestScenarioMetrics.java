package com.yepot.observability.test.metric;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class TestScenarioMetrics {

    public static final String SCENARIO_REQUESTS_METRIC = "observability.test.scenario.requests";
    public static final String SCENARIO_DURATION_METRIC = "observability.test.scenario.duration";
    public static final String MASS_TRANSFER_ATTEMPTS_METRIC = "observability.test.mass_transfer.attempts";
    public static final String MASS_TRANSFER_REQUEST_SIZE_METRIC = "observability.test.mass_transfer.request_size";
    public static final String MASS_TRANSFER_SUCCESS_SIZE_METRIC = "observability.test.mass_transfer.success_size";
    public static final String MASS_TRANSFER_FAILURE_SIZE_METRIC = "observability.test.mass_transfer.failure_size";
    public static final String MASS_TRANSFER_AMOUNT_METRIC = "observability.test.mass_transfer.amount";

    private final MeterRegistry meterRegistry;

    public TestScenarioMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public <T> T recordScenario(String scenario, Supplier<T> action) {
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            T result = action.get();
            recordScenarioOutcome(scenario, "success", sample);
            return result;
        }
        catch (RuntimeException exception) {
            recordScenarioOutcome(scenario, "failure", sample);
            throw exception;
        }
    }

    public void recordMassTransferAttempt(boolean success) {
        meterRegistry.counter(
            MASS_TRANSFER_ATTEMPTS_METRIC,
            "result",
            success ? "success" : "failure"
        ).increment();
    }

    public void recordMassTransferBatch(int requestedCount, int successCount, int failCount, long totalAmount) {
        summary(MASS_TRANSFER_REQUEST_SIZE_METRIC, "Requested transfer count per mass-transfer call")
            .record(requestedCount);
        summary(MASS_TRANSFER_SUCCESS_SIZE_METRIC, "Successful transfer count per mass-transfer call")
            .record(successCount);
        summary(MASS_TRANSFER_FAILURE_SIZE_METRIC, "Failed transfer count per mass-transfer call")
            .record(failCount);
        summary(MASS_TRANSFER_AMOUNT_METRIC, "Transferred amount per successful mass-transfer call")
            .record(totalAmount);
    }

    private void recordScenarioOutcome(String scenario, String result, Timer.Sample sample) {
        meterRegistry.counter(
            SCENARIO_REQUESTS_METRIC,
            "scenario",
            scenario,
            "result",
            result
        ).increment();

        sample.stop(
            Timer.builder(SCENARIO_DURATION_METRIC)
                .description("Duration of observability test scenarios")
                .publishPercentileHistogram()
                .tag("scenario", scenario)
                .tag("result", result)
                .register(meterRegistry)
        );
    }

    private DistributionSummary summary(String name, String description) {
        return DistributionSummary.builder(name)
            .description(description)
            .publishPercentileHistogram()
            .register(meterRegistry);
    }
}

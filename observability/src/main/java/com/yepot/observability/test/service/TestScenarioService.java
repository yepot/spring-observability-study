package com.yepot.observability.test.service;

import com.yepot.observability.global.exception.ApiException;
import com.yepot.observability.global.exception.ExceptionCode;
import com.yepot.observability.test.dto.request.MassTransferRequest;
import com.yepot.observability.test.dto.response.ErrorTransferResponse;
import com.yepot.observability.test.dto.response.MassTransferResponse;
import com.yepot.observability.test.dto.response.SlowTransferResponse;
import com.yepot.observability.test.metric.TestScenarioMetrics;
import com.yepot.observability.transfer.dto.request.TransferRequest;
import com.yepot.observability.transfer.service.TransferService;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class TestScenarioService {

    private static final long SLOW_TRANSFER_DELAY_MS = 3000L;
    private static final String SLOW_TRANSFER_SCENARIO = "slow-transfer";
    private static final String ERROR_TRANSFER_SCENARIO = "error-transfer";
    private static final String MASS_TRANSFER_SCENARIO = "mass-transfer";

    private final TransferService transferService;
    private final TestScenarioMetrics testScenarioMetrics;

    public TestScenarioService(
        TransferService transferService,
        TestScenarioMetrics testScenarioMetrics
    ) {
        this.transferService = transferService;
        this.testScenarioMetrics = testScenarioMetrics;
    }

    public SlowTransferResponse slowTransfer() {
        return testScenarioMetrics.recordScenario(SLOW_TRANSFER_SCENARIO, () -> {
            try {
                Thread.sleep(SLOW_TRANSFER_DELAY_MS);
            }
            catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new ApiException(ExceptionCode.SLOW_TRANSFER_FAILED);
            }

            return new SlowTransferResponse("slow transfer response", SLOW_TRANSFER_DELAY_MS);
        });
    }

    public ErrorTransferResponse errorTransfer() {
        return testScenarioMetrics.recordScenario(ERROR_TRANSFER_SCENARIO, () -> {
            throw new ApiException(ExceptionCode.ERROR_TRANSFER_FAILED);
        });
    }

    public MassTransferResponse massTransfer(MassTransferRequest request) {
        return testScenarioMetrics.recordScenario(MASS_TRANSFER_SCENARIO, () -> {
            validateMassTransferRequest(request);

            long startNanos = System.nanoTime();
            int successCount = 0;
            int failCount = 0;
            long totalAmount = 0L;

            for (int index = 0; index < request.count(); index++) {
                try {
                    transferService.transfer(
                        new TransferRequest(request.fromAccountId(), request.toAccountId(), request.amount())
                    );
                    successCount++;
                    totalAmount += request.amount();
                    testScenarioMetrics.recordMassTransferAttempt(true);
                }
                catch (ApiException exception) {
                    failCount++;
                    testScenarioMetrics.recordMassTransferAttempt(false);
                }
            }

            long durationMs = Math.max(1L, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos));
            testScenarioMetrics.recordMassTransferBatch(request.count(), successCount, failCount, totalAmount);

            return new MassTransferResponse(
                request.count(),
                successCount,
                failCount,
                totalAmount,
                durationMs
            );
        });
    }

    private void validateMassTransferRequest(MassTransferRequest request) {
        if (request == null) {
            throw new ApiException(ExceptionCode.REQUEST_BODY_REQUIRED);
        }

        if (request.fromAccountId() == null) {
            throw new ApiException(ExceptionCode.FROM_ACCOUNT_ID_REQUIRED);
        }

        if (request.toAccountId() == null) {
            throw new ApiException(ExceptionCode.TO_ACCOUNT_ID_REQUIRED);
        }

        if (request.amount() == null) {
            throw new ApiException(ExceptionCode.TRANSFER_AMOUNT_REQUIRED);
        }

        if (request.amount() <= 0) {
            throw new ApiException(ExceptionCode.TRANSFER_AMOUNT_INVALID);
        }

        if (request.count() == null) {
            throw new ApiException(ExceptionCode.MASS_TRANSFER_COUNT_REQUIRED);
        }

        if (request.count() < 1) {
            throw new ApiException(ExceptionCode.MASS_TRANSFER_COUNT_INVALID);
        }
    }
}

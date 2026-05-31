package com.yepot.observability.test.controller;

import com.yepot.observability.test.dto.request.MassTransferRequest;
import com.yepot.observability.test.dto.response.ErrorTransferResponse;
import com.yepot.observability.test.dto.response.MassTransferResponse;
import com.yepot.observability.test.dto.response.SlowTransferResponse;
import com.yepot.observability.test.service.TestScenarioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestScenarioController {

    private final TestScenarioService testScenarioService;

    public TestScenarioController(TestScenarioService testScenarioService) {
        this.testScenarioService = testScenarioService;
    }

    @GetMapping("/test/slow-transfer")
    public SlowTransferResponse slowTransfer() {
        return testScenarioService.slowTransfer();
    }

    @GetMapping("/test/error-transfer")
    public ErrorTransferResponse errorTransfer() {
        return testScenarioService.errorTransfer();
    }

    @PostMapping("/test/mass-transfer")
    public MassTransferResponse massTransfer(@RequestBody MassTransferRequest request) {
        return testScenarioService.massTransfer(request);
    }
}

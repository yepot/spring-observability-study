package com.yepot.observability.transfer.controller;

import com.yepot.observability.transfer.dto.request.TransferRequest;
import com.yepot.observability.transfer.dto.response.TransferResponse;
import com.yepot.observability.transfer.service.TransferService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping({"/transfer", "/transfers"})
    public TransferResponse transfer(@RequestBody TransferRequest request) {
        return transferService.transfer(request);
    }
}

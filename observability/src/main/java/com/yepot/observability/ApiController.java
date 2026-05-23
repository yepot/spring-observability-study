package com.yepot.observability;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

    @GetMapping("/1")
    public String test1() {
        return "Test API 1";
    }

    @GetMapping("/2")
    public String test2() {
        return "Test API 1";
    }
}

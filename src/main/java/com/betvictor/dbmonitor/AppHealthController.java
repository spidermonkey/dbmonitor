package com.betvictor.dbmonitor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppHealthController {

    @GetMapping("/apphealth")
    public String alive() throws Exception {
        return "OK";
    }
}

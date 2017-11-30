package com.betvictor.dbmonitor;

import com.betvictor.dbmonitor.config.DbMonitorProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppHealthController {

    @Autowired
    private DbMonitorProperties dbMonitorProperties;

    @GetMapping("/apphealth")
    public String alive()  {
        return "OK";
    }

    @GetMapping("/version")
    public String version() {
        return dbMonitorProperties.getVersion();
    }
}

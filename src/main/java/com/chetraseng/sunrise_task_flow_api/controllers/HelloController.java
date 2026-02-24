package com.chetraseng.sunrise_task_flow_api.controllers;

import com.chetraseng.sunrise_task_flow_api.services.CountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
  private final CountService countService;

  public HelloController(CountService countService) {
    this.countService = countService;
  }

  @GetMapping("/hello")
  public String getHello() {
    countService.increment();
    return "Hello, Spring Boot! Count: " + countService.getCount();
  }
}

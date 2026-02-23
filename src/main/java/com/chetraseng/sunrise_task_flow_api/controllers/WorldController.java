package com.chetraseng.sunrise_task_flow_api.controllers;

import com.chetraseng.sunrise_task_flow_api.services.CountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorldController {
  private final CountService countService;

  public WorldController(CountService countService) {
    this.countService = countService;
  }

  @GetMapping("/world")
  public String getWorld() {
    countService.increment();
    return "This is my world! Count: " + countService.getCount();
  }
}

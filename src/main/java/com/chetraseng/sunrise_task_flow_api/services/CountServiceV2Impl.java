package com.chetraseng.sunrise_task_flow_api.services;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!test")
public class CountServiceV2Impl implements CountService {
  private int count = 100;

  @Override
  public void increment() {
    count++;
  }

  @Override
  public int getCount() {
    return count;
  }
}

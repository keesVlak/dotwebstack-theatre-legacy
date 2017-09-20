package org.dotwebstack.theatre.application;

import com.google.common.collect.ImmutableMap;
import org.dotwebstack.theatre.application.config.Constants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("org.dotwebstack")
public class TheatreApplication {

  public static void main(String[] args) {
    SpringApplication springApplication = new SpringApplication(TheatreApplication.class);
    springApplication.setDefaultProperties(ImmutableMap.of(
        Constants.SPRING_PROFILE_DEFAULT_PROPERTY, Constants.SPRING_PROFILE_PRODUCTION));
    springApplication.run(args);
  }

}

package org.dotwebstack.theatre.application;

import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.theatre.application.config.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

@SpringBootApplication
@ComponentScan("org.dotwebstack")
public class TheatreApplication {

  private static final Logger LOG = LoggerFactory.getLogger(TheatreApplication.class);

  private static final String SPRING_PROFILE_DEFAULT = "spring.profiles.default";

  private static Environment environment;

  public TheatreApplication(Environment environment) {
    this.environment = environment;
  }

  public static void main(String[] args) {
    SpringApplication springApplication = new SpringApplication(TheatreApplication.class);
    addDefaultProfile(springApplication);
    environment = springApplication.run(args).getEnvironment();
  }

  private static void addDefaultProfile(SpringApplication springApplication) {
    Map<String, Object> defaultProperties = new HashMap<>();
    defaultProperties.put(SPRING_PROFILE_DEFAULT, Constants.SPRING_PROFILE_PRODUCTION);
    springApplication.setDefaultProperties(defaultProperties);
  }

}

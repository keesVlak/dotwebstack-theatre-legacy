package org.dotwebstack.theatre.application.config;

public class Constants {

  public static final String SPRING_PROFILE_DEFAULT_PROPERTY = "spring.profiles.default";

  public static final String SPRING_PROFILE_DEVELOPMENT = "development";

  public static final String SPRING_PROFILE_PRODUCTION = "production";

  private Constants() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", Constants.class));
  }
}

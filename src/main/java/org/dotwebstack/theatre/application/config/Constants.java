package org.dotwebstack.theatre.application.config;

public class Constants {

  public final static String SPRING_PROFILE_DEVELOPMENT = "development";

  public final static String SPRING_PROFILE_PRODUCTION = "production";

  public final static String SPRING_PROFILE_TEST = "test";

  private Constants() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", Constants.class));
  }
}

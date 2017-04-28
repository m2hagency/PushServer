package org.whispersystems.pushserver;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.whispersystems.pushserver.config.ApnConfiguration;
import org.whispersystems.pushserver.config.AuthenticationConfiguration;
import org.whispersystems.pushserver.config.RedisConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.dropwizard.Configuration;

public class PushServerConfiguration extends Configuration {

  @JsonProperty
  @Valid
  @NotNull
  private AuthenticationConfiguration authentication;

  @JsonProperty
  @Valid
  @NotNull
  private RedisConfiguration redis;

  @JsonProperty
  @Valid
  @NotNull
  private ApnConfiguration apn;

  public AuthenticationConfiguration getAuthenticationConfiguration() {
    return authentication;
  }

  public RedisConfiguration getRedisConfiguration() {
    return redis;
  }

  public ApnConfiguration getApnConfiguration() {
    return apn;
  }

}

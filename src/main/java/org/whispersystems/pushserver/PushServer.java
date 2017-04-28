package org.whispersystems.pushserver;

import com.codahale.metrics.SharedMetricRegistries;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.pushserver.auth.Server;
import org.whispersystems.pushserver.auth.ServerAuthenticator;
import org.whispersystems.pushserver.config.ApnConfiguration;
import org.whispersystems.pushserver.controllers.FeedbackController;
import org.whispersystems.pushserver.controllers.PushController;
import org.whispersystems.pushserver.providers.RedisClientFactory;
import org.whispersystems.pushserver.providers.RedisHealthCheck;
import org.whispersystems.pushserver.senders.APNSender;
import org.whispersystems.pushserver.senders.UnregisteredQueue;
import org.whispersystems.pushserver.util.Constants;

import java.security.Security;
import java.util.List;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.basic.BasicAuthFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import redis.clients.jedis.JedisPool;

public class PushServer extends Application<PushServerConfiguration> {

  private final Logger logger = LoggerFactory.getLogger(PushServer.class);

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  @Override
  public void initialize(Bootstrap<PushServerConfiguration> bootstrap) {}

  @Override
  public void run(PushServerConfiguration config, Environment environment) throws Exception {
    SharedMetricRegistries.add(Constants.METRICS_NAME, environment.metrics());
    environment.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    JedisPool           redisClient         = new RedisClientFactory(config.getRedisConfiguration()).getRedisClientPool();
    ServerAuthenticator serverAuthenticator = new ServerAuthenticator(config.getAuthenticationConfiguration());
    List<Server>        servers             = config.getAuthenticationConfiguration().getServers();
    UnregisteredQueue   apnQueue            = new UnregisteredQueue(redisClient, environment.getObjectMapper(), servers, "apn");

    APNSender apnSender = initializeApnSender(redisClient, apnQueue, config.getApnConfiguration());

    environment.lifecycle().manage(apnSender);

    environment.jersey().register(AuthFactory.binder(new BasicAuthFactory<>(serverAuthenticator, "PushServer", Server.class)));
    environment.jersey().register(new PushController(apnSender));
    environment.jersey().register(new FeedbackController(apnQueue));

    environment.healthChecks().register("Redis", new RedisHealthCheck(redisClient));
  }

  private APNSender initializeApnSender(JedisPool redisClient,
                                        UnregisteredQueue apnQueue,
                                        ApnConfiguration configuration)
  {
    return new APNSender(redisClient, apnQueue,
                         configuration.getPushCertificate(),
                         configuration.getPushKey(),
                         configuration.isFeedbackEnabled(),
                         configuration.isSandboxEnabled());
  }

  public static void main(String[] args) throws Exception {
    new PushServer().run(args);
  }
}

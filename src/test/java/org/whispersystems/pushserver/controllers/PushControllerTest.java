package org.whispersystems.pushserver.controllers;

import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.whispersystems.pushserver.auth.Server;
import org.whispersystems.pushserver.auth.ServerAuthenticator;
import org.whispersystems.pushserver.entities.ApnMessage;
import org.whispersystems.pushserver.senders.APNSender;
import org.whispersystems.pushserver.senders.TransientPushFailureException;
import org.whispersystems.pushserver.util.AuthHelper;
import org.whispersystems.pushserver.util.MockAuthenticationConfig;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.basic.BasicAuthFactory;
import io.dropwizard.testing.junit.ResourceTestRule;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PushControllerTest {

  private static final APNSender apnSender = mock(APNSender.class);

  @ClassRule
  public static final ResourceTestRule resources =
      ResourceTestRule.builder()
                      .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
                      .addResource(new PushController(apnSender))
                      .addProvider(AuthFactory.binder(new BasicAuthFactory<>(new ServerAuthenticator(new MockAuthenticationConfig()), "TEST", Server.class)))
                      .build();

  @Test
  public void testSendApn() throws TransientPushFailureException {
    Response response = resources.getJerseyTest().target("/api/v1/push/apn/")
                                 .request()
                                 .header("Authorization", AuthHelper.getAuthHeader("textsecure", "foobar"))
                                 .put(Entity.entity(new ApnMessage("12345", "+14152222222", 1, "Hey there!", 1111, false), MediaType.APPLICATION_JSON));

    assertThat(response.getStatus()).isEqualTo(204);

    ArgumentCaptor<ApnMessage> captor = ArgumentCaptor.forClass(ApnMessage.class);
    verify(apnSender).sendMessage(captor.capture());

    assertThat(captor.getValue().getApnId()).isEqualTo("12345");
    assertThat(captor.getValue().getDeviceId()).isEqualTo(1);
    assertThat(captor.getValue().getNumber()).isEqualTo("+14152222222");
    assertThat(captor.getValue().getMessage()).isEqualTo("Hey there!");
    assertThat(captor.getValue().getExpirationTime()).isEqualTo(1111);
  }

  @Test
  public void testUnauthorizedSendApn() throws TransientPushFailureException {
    Response response = resources.getJerseyTest().target("/api/v1/push/apn/")
                                 .request()
                                 .header("Authorization", AuthHelper.getAuthHeader("redphone", "foobar"))
                                 .put(Entity.entity(new ApnMessage("12345", "+14152222222", 1, "Hey there!", 2222, false), MediaType.APPLICATION_JSON), Response.class);

    assertThat(response.getStatus()).isEqualTo(401);
    verifyNoMoreInteractions(apnSender);
  }

}

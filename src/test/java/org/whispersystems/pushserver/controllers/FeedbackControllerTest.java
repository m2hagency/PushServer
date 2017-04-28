package org.whispersystems.pushserver.controllers;

import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.whispersystems.pushserver.auth.Server;
import org.whispersystems.pushserver.auth.ServerAuthenticator;
import org.whispersystems.pushserver.entities.UnregisteredEvent;
import org.whispersystems.pushserver.entities.UnregisteredEventList;
import org.whispersystems.pushserver.senders.UnregisteredQueue;
import org.whispersystems.pushserver.util.AuthHelper;
import org.whispersystems.pushserver.util.MockAuthenticationConfig;

import javax.ws.rs.core.Response;
import java.util.LinkedList;
import java.util.List;

import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.basic.BasicAuthFactory;
import io.dropwizard.testing.junit.ResourceTestRule;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FeedbackControllerTest {

  private static final UnregisteredQueue apnQueue = mock(UnregisteredQueue.class);

  @ClassRule
  public static final ResourceTestRule resources =
      ResourceTestRule.builder()
                      .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
                      .addProvider(AuthFactory.binder(new BasicAuthFactory<>(new ServerAuthenticator(new MockAuthenticationConfig()), "TEST", Server.class)))
                      .addResource(new FeedbackController(apnQueue))
                      .build();

  @Before
  public void setup() {
    List<UnregisteredEvent> apnEvents = new LinkedList<UnregisteredEvent>() {{
      add(new UnregisteredEvent("6666", null, "+14151231234", 1, System.currentTimeMillis()));
      add(new UnregisteredEvent("7777", null, "+14154444444", 1, System.currentTimeMillis()));
    }};

    when(apnQueue.get(anyString())).thenReturn(apnEvents);
  }

  @Test
  public void testApnFeedback() {
    Response clientResponse = resources.getJerseyTest().target("/api/v1/feedback/apn")
                                       .request()
                                       .header("Authorization", AuthHelper.getAuthHeader("redphone", "foobaz"))
                                       .get(Response.class);

    assertThat(clientResponse.getStatus()).isEqualTo(200);

    UnregisteredEventList list = clientResponse.readEntity(UnregisteredEventList.class);
    assertThat(list.getDevices().size()).isEqualTo(2);

    assertThat(list.getDevices().get(0).getRegistrationId()).isEqualTo("6666");
    assertThat(list.getDevices().get(0).getNumber()).isEqualTo("+14151231234");
    assertThat(list.getDevices().get(0).getDeviceId()).isEqualTo(1);

    assertThat(list.getDevices().get(1).getRegistrationId()).isEqualTo("7777");
    assertThat(list.getDevices().get(1).getNumber()).isEqualTo("+14154444444");
    assertThat(list.getDevices().get(1).getDeviceId()).isEqualTo(1);
  }

  @Test
  public void testApnFeedbackUnauthorized() {
    Response clientResponse = resources.getJerseyTest().target("/api/v1/feedback/apn")
                                       .request()
                                       .header("Authorization", AuthHelper.getAuthHeader("something", "foobar"))
                                       .get(Response.class);

    assertThat(clientResponse.getStatus()).isEqualTo(401);
  }
}

package us.arvatosystems.com.yaas.service.message;

import java.io.IOException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import us.arvatosystems.com.yaas.domain.PubSubEvent;
import us.arvatosystems.com.yaas.domain.PubSubReadResponse;
import us.arvatosystems.com.yaas.domain.SMSMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.cloud.yaas.api.HybrisPubSubServiceApiClient;
import com.sap.cloud.yaas.servicesdk.authorization.AccessToken;
import com.sap.cloud.yaas.servicesdk.authorization.AuthorizationScope;
import com.sap.cloud.yaas.servicesdk.authorization.DiagnosticContext;
import com.sap.cloud.yaas.servicesdk.authorization.integration.AuthorizedExecutionCallback;
import com.sap.cloud.yaas.servicesdk.authorization.integration.AuthorizedExecutionTemplate;

@Component
public class IncomingMessagePoller
{
	private static final Logger LOG = LoggerFactory.getLogger(IncomingMessagePoller.class);

	private static final String PUBSUB_TOPIC_OWNER = "arvato.sms";
	private static final String PUBSUB_EVENT_TYPE = "sms_incoming";

	@Value("${TENANT}")
	private String tenant;

	@Autowired
	private AuthorizedExecutionTemplate authTemplate;

	@Autowired
	private HybrisPubSubServiceApiClient client;

	@Autowired
	private ApplicationEventPublisher publisher;

	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Reads new messages from the PubSub service every 5 seconds and publishes and event for each new message.
	 */
	@Scheduled(fixedDelay = 5000)
	public void checkForNewMessages()
	{
		LOG.info("Checking for new messages...");

		final Response response = authTemplate.executeAuthorized(new AuthorizationScope(), new DiagnosticContext(
				"not implemented yet", Integer.valueOf(0)),
				new AuthorizedExecutionCallback<Response>()
				{
					@Override
					public Response execute(final AccessToken token)
					{
						return client.topics().topicOwnerClientEventType(PUBSUB_TOPIC_OWNER, PUBSUB_EVENT_TYPE).read().preparePost()
								.withHeader("Authorization", "Bearer " + token.getValue())
								.withHeader("Content-type", "application/json").withHeader("Accept", "application/json").execute();
					}
				});

		if (response.getStatusInfo().getFamily().equals(Status.Family.SUCCESSFUL))
		{
			LOG.debug("PubSub read reuest was successfull (Status: {})", response.getStatus());
			processNewMessages(response.readEntity(PubSubReadResponse.class));
		}
		else
		{
			LOG.error("Unable to read messages from PubSub because of {} {}", response.getStatus(),
					response.readEntity(String.class));
		}
	}

	protected void processNewMessages(final PubSubReadResponse response)
	{
		LOG.debug("Retrieved {} events", response.getEvents().size());

		for (final PubSubEvent event : response.getEvents())
		{
			try
			{
				final SMSMessage message = objectMapper.readValue(event.getPayload(), SMSMessage.class);
				LOG.info("Processing new incoming message from '{}' with test '{}'", message.getFromNumber(),
						message.getMessageText());
				publisher.publishEvent(new IncomingMessageEvent(this, message.getFromNumber(), message.getToNumber(), message
						.getMessageText()));
			}
			catch (final IOException e)
			{
				LOG.error("Unable to understand payload format of message " + response.getId(), e);
			}
		}
	}

}

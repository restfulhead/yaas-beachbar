package us.arvatosystems.com.yaas.service.message;

import java.io.IOException;
import java.util.Collections;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import us.arvatosystems.com.yaas.Util;
import us.arvatosystems.com.yaas.domain.PubSubEvent;
import us.arvatosystems.com.yaas.domain.PubSubReadResponse;
import us.arvatosystems.com.yaas.domain.PubSubReadSettings;
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

	@Value("${TENANT}")
	private String tenant;

	@Value("${polling.disabled:false}")
	private boolean pollingDisabled;

	@Value("${pubsub.topic}")
	private String pubSubTopic;

	@Autowired
	private AuthorizedExecutionTemplate authTemplate;

	@Autowired
	private HybrisPubSubServiceApiClient pubSubClient;

	@Autowired
	private ApplicationEventPublisher publisher;

	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Reads new messages from the PubSub service every 5 seconds and publishes and event for each new message.
	 */
	@Scheduled(fixedDelay = 10000)
	public void checkForNewMessages()
	{
		if (!pollingDisabled)
		{
			final String scope = "hybris.pubsub.topic=" + PUBSUB_TOPIC_OWNER + "." + pubSubTopic;
			LOG.debug("Checking for new messages under {}", scope);

			final Response response = authTemplate.executeAuthorized(new AuthorizationScope(Collections.singletonList(scope)),
					new DiagnosticContext("not implemented yet", Integer.valueOf(0)), new AuthorizedExecutionCallback<Response>()
			{
				@Override
				public Response execute(final AccessToken token)
				{
					return pubSubClient.topics().topicOwnerClientEventType(PUBSUB_TOPIC_OWNER, pubSubTopic).read().preparePost()
							.withPayload(Entity.entity(new PubSubReadSettings(10, 5000, true), MediaType.APPLICATION_JSON))
							.withHeader("Authorization", "Bearer " + token.getValue()).withHeader("Content-type", "application/json")
							.withHeader("Accept", "application/json").execute();
				}
			});

			if (response.getStatusInfo().getFamily().equals(Status.Family.SUCCESSFUL))
			{
				if (response.getStatus() == 204)
				{
					LOG.info("PubSub read reuest was successfull (Status: {}). No new messages", response.getStatus());
				}
				else
				{
					final PubSubReadResponse pubSubResponse = response.readEntity(PubSubReadResponse.class);
					LOG.info("PubSub read reuest was successfull (Status: {}). {} new event(s)", response.getStatus(), pubSubResponse
							.getEvents().size());

					processNewMessages(pubSubResponse);
				}
			}
			else
			{
				LOG.error("Unable to read messages from PubSub because of {} {}", response.getStatus(),
						response.readEntity(String.class));
			}
		}
	}

	protected void processNewMessages(final PubSubReadResponse response)
	{
		for (final PubSubEvent event : response.getEvents())
		{
			try
			{
				final SMSMessage message = objectMapper.readValue(event.getPayload(), SMSMessage.class);
				LOG.info("Processing new incoming message from '{}' with text '{}'", Util.maskPhoneNo(message.getFromNumber()),
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

	public void setAuthTemplate(final AuthorizedExecutionTemplate authTemplate)
	{
		this.authTemplate = authTemplate;
	}

	public void setPublisher(final ApplicationEventPublisher publisher)
	{
		this.publisher = publisher;
	}

	public void setObjectMapper(final ObjectMapper objectMapper)
	{
		this.objectMapper = objectMapper;
	}

}

package us.arvatosystems.com.yaas.service.message;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import us.arvatosystems.com.yaas.helper.Util;

import com.sap.cloud.yaas.api.ArvatoSmsServiceClient;
import com.sap.cloud.yaas.servicesdk.authorization.AccessToken;
import com.sap.cloud.yaas.servicesdk.authorization.AuthorizationScope;
import com.sap.cloud.yaas.servicesdk.authorization.integration.AuthorizedExecutionCallback;
import com.sap.cloud.yaas.servicesdk.authorization.integration.AuthorizedExecutionTemplate;

@Component
public class OutgoingMessageProcessor implements ApplicationListener<OutgoingMessageEvent>
{
	private static final Logger LOG = LoggerFactory.getLogger(OutgoingMessageProcessor.class);

	@Autowired
	private ArvatoSmsServiceClient smsClient;

	@Autowired
	private AuthorizedExecutionTemplate authTemplate;

	/**
	 * Calls the arvato SMS service to send an SMS to the client.
	 *
	 * @param event the event including message details
	 */
	@Override
	public void onApplicationEvent(final OutgoingMessageEvent event)
	{
		LOG.info("Sending new message to '{}' with text '{}'.", Util.maskPhoneNo(event.getMessage().getToNumber()), event
				.getMessage().getMessageText());

		final Response response = authTemplate.executeAuthorized(new AuthorizationScope(), Util.newContext(),
				new AuthorizedExecutionCallback<Response>()
		{
			@Override
			public Response execute(final AccessToken token)
			{
				return smsClient.sms().preparePost().withHeader("Authorization", "Bearer " + token.getValue())
						.withHeader("Content-type", "application/json").withHeader("Accept", "application/json")
						.withPayload(event.getMessage()).execute();
			}
		});

		if (response.getStatusInfo().getFamily().equals(Status.Family.SUCCESSFUL))
		{
			LOG.debug("Message successfully sent to {} (Status: {})", Util.maskPhoneNo(event.getMessage().getToNumber()),
					response.getStatus());
		}
		else
		{
			LOG.error("Unable to sent message to {} because of {} {}", Util.maskPhoneNo(event.getMessage().getToNumber()),
					response.getStatus(),
					response.readEntity(String.class));
		}
	}

}

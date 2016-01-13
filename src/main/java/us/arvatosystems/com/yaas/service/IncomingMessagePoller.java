package us.arvatosystems.com.yaas.service;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sap.cloud.yaas.api.ArvatoSmsServiceClient;
import com.sap.cloud.yaas.servicesdk.authorization.AccessToken;
import com.sap.cloud.yaas.servicesdk.authorization.AuthorizationScope;
import com.sap.cloud.yaas.servicesdk.authorization.DiagnosticContext;
import com.sap.cloud.yaas.servicesdk.authorization.integration.AuthorizedExecutionCallback;
import com.sap.cloud.yaas.servicesdk.authorization.integration.AuthorizedExecutionTemplate;

@Component
public class IncomingMessagePoller
{
	private static final Logger LOG = LoggerFactory.getLogger(IncomingMessagePoller.class);

	@Value("${TENANT}")
	private String tenant;

	@Autowired
	private AuthorizedExecutionTemplate authTemplate;

	@Autowired
	private ArvatoSmsServiceClient smsClient;

	@Scheduled(fixedDelay = 5000)
	public void checkForNewMessages()
	{
		LOG.info("Checking for new messages...");

		final Response response = authTemplate.executeAuthorized(new AuthorizationScope(), new DiagnosticContext("todo", 0),
				new AuthorizedExecutionCallback<Response>()
				{
					@Override
					public Response execute(final AccessToken token)
					{
						return smsClient.sms().prepareGet().withHeader("Authorization", "Bearer " + token.getValue())
								.withHeader("Content-type", "application/json").withHeader("Accept", "application/json").execute();
					}
				});

		System.out.println(response.getStatus());
	}
}

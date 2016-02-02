package us.arvatosystems.com.yaas.service.checkout;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import us.arvatosystems.com.yaas.domain.Customer;
import us.arvatosystems.com.yaas.domain.Product;
import us.arvatosystems.com.yaas.domain.SalesOrder;
import us.arvatosystems.com.yaas.domain.SalesOrderEntry;
import us.arvatosystems.com.yaas.helper.Util;

import com.sap.cloud.yaas.api.OrderServiceClient;
import com.sap.cloud.yaas.servicesdk.authorization.AccessToken;
import com.sap.cloud.yaas.servicesdk.authorization.integration.AuthorizedExecutionCallback;
import com.sap.cloud.yaas.servicesdk.authorization.integration.AuthorizedExecutionTemplate;

/**
 * A very basic interface to the YaaS order service. For now this is just a proof of concept with a lot of hard-coded information.
 */
@Component
public class OrderService
{
	private static final Logger LOG = LoggerFactory.getLogger(OrderService.class);

	private static final String SCOPE_CREATE_ORDER = "hybris.order_create";

	@Autowired
	private OrderServiceClient orderServiceClient;

	@Value("${TENANT}")
	private String tenant;

	@Autowired
	private AuthorizedExecutionTemplate authTemplate;

	public void placeOrder(final List<Product> products, final String phoneNo)
	{
		LOG.info("Sending new order for {} to YaaS", phoneNo);

		final Response response = authTemplate.executeAuthorized(Util.newScope(SCOPE_CREATE_ORDER), Util.newContext(),
				new AuthorizedExecutionCallback<Response>()
				{
					@Override
					public Response execute(final AccessToken token)
					{
						final List<SalesOrderEntry> entries = products.stream().map(i -> new SalesOrderEntry(i, 1))
								.collect(Collectors.toList());
						final SalesOrder order = new SalesOrder(entries, new Customer(phoneNo, "noreply@arvatosystems.io"));


						return orderServiceClient.tenant(tenant).salesorders().preparePost()
								.withPayload(Entity.entity(order, MediaType.APPLICATION_JSON))
								.withHeader("Authorization", "Bearer " + token.getValue()).withHeader("Content-type", "application/json")
								.withHeader("Accept", "application/json").execute();
					}
				});

		if (response.getStatusInfo().getFamily().equals(Status.Family.SUCCESSFUL))
		{
			if (response.getStatus() == 201)
			{
				LOG.info("Order POST was successful (Status: {}). New order available at: {}", response.getStatus(), response
						.getLocation().toString());
			}
			else
			{
				LOG.warn("Order POST was successfull, however status {} is is unexpected. Response body: {}", response.getStatus(),
						response.readEntity(String.class));
			}
		}
		else
		{
			throw new IllegalStateException("Order creation failed. Status " + response.getStatus() + ", Details: "
					+ response.readEntity(String.class));
		}
	}
}

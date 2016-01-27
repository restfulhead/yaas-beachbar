package us.arvatosystems.com.yaas.service.checkout;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import us.arvatosystems.com.yaas.domain.Product;
import us.arvatosystems.com.yaas.domain.SalesOrder;
import us.arvatosystems.com.yaas.domain.SalesOrderEntry;

import com.sap.cloud.yaas.api.OrderServiceClient;
import com.sap.cloud.yaas.servicesdk.authorization.AccessToken;
import com.sap.cloud.yaas.servicesdk.authorization.AuthorizationScope;
import com.sap.cloud.yaas.servicesdk.authorization.DiagnosticContext;
import com.sap.cloud.yaas.servicesdk.authorization.integration.AuthorizedExecutionCallback;
import com.sap.cloud.yaas.servicesdk.authorization.integration.AuthorizedExecutionTemplate;

/**
 * A very basic interface to the YaaS order service. For now this is just a proof of concept with a lot of hard-coded information.
 */
@Component
public class OrderService
{
	private static final String COPE_CREATE_ORDER = "hybris.order_create";

	@Autowired
	private OrderServiceClient orderServiceClient;

	@Value("${TENANT}")
	private String tenant;

	@Autowired
	private AuthorizedExecutionTemplate authTemplate;

	public void placeOrder(final List<Product> products)
	{
		final Response response = authTemplate.executeAuthorized(
				new AuthorizationScope(Collections.singletonList(COPE_CREATE_ORDER)), new DiagnosticContext("not implemented yet",
						Integer.valueOf(0)), new AuthorizedExecutionCallback<Response>()
				{
					@Override
					public Response execute(final AccessToken token)
					{
						final List<SalesOrderEntry> entries = products.stream().map(i -> new SalesOrderEntry(i, 1))
								.collect(Collectors.toList());
						final SalesOrder order = new SalesOrder(entries);

						return orderServiceClient.tenant(tenant).salesorders().preparePost()
								.withPayload(Entity.entity(order, MediaType.APPLICATION_JSON))
								.withHeader("Authorization", "Bearer " + token.getValue()).withHeader("Content-type", "application/json")
								.withHeader("Accept", "application/json").execute();
					}
				});

		System.out.println(response);
	}
}

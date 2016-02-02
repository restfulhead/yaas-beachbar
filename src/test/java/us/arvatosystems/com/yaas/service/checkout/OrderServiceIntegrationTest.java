package us.arvatosystems.com.yaas.service.checkout;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import us.arvatosystems.com.yaas.domain.Product;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/applicationContext.xml" })
public class OrderServiceIntegrationTest
{
	@Autowired
	private OrderService orderService;

	@Test
	public void shouldPlaceOrder()
	{
		final List<Product> products = new ArrayList<>();
		products.add(new Product("Beer", "beer", 4.50));
		products.add(new Product("Water", "water", 2));

		orderService.placeOrder(products, "+123123412345");
	}
}

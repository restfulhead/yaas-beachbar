package us.arvatosystems.com.yaas.service.rule;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import us.arvatosystems.com.yaas.domain.Product;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/applicationContext.xml" })
public class RulesEngineTest
{
	@Autowired
	private RulesEngineService rulesEngine;

	@Test
	public void shouldIdentifyBeer1()
	{
		final String input = "I want a beer!";
		final List<Product> beverages = rulesEngine.identifyBeverages(input);
		assertThat(beverages.size(), is(1));
		assertThat(beverages.get(0).getName(), equalTo("Beer"));
	}

	@Test
	public void shouldIdentifyBeer2()
	{
		final String input = "🍺";
		final List<Product> beverages = rulesEngine.identifyBeverages(input);
		assertThat(beverages.size(), is(1));
		assertThat(beverages.get(0).getName(), equalTo("Beer"));
	}

	@Test
	public void shouldIdentifyBeer3()
	{
		final String input = "🍻 please";
		final List<Product> beverages = rulesEngine.identifyBeverages(input);
		assertThat(beverages.size(), is(1));
		assertThat(beverages.get(0).getName(), equalTo("Beer"));
	}

	@Test
	public void shouldIdentifyWater()
	{
		final String input = "A water please";
		final List<Product> beverages = rulesEngine.identifyBeverages(input);
		assertThat(beverages.size(), is(1));
		assertThat(beverages.get(0).getName(), equalTo("Water"));
	}

	@Test
	public void shouldIdentifyBeerAndWater()
	{
		final String input = "A beer and a water please";
		final List<Product> beverages = rulesEngine.identifyBeverages(input);
		assertThat(beverages.size(), is(2));
		assertThat(beverages.get(0).getName(), equalTo("Beer"));
		assertThat(beverages.get(1).getName(), equalTo("Water"));
	}

}

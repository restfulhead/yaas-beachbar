package us.arvatosystems.com.yaas.service.rule;

import java.util.ArrayList;
import java.util.List;

import org.easyrules.api.RulesEngine;
import org.easyrules.core.RulesEngineBuilder;
import org.springframework.stereotype.Component;

import us.arvatosystems.com.yaas.domain.Product;

@Component
public class RulesEngineService
{
	public List<Product> identifyBeverages(final String input)
	{
		// prepare
		final List<BeverageRule> rules = new ArrayList<>();
		rules.add(new BeerRule(input));
		rules.add(new WaterRule(input));
		rules.add(new WineRule(input));
		rules.add(new CocktailRule(input));
		rules.add(new CoffeeRule(input));

		final RulesEngine rulesEngine = RulesEngineBuilder.aNewRulesEngine().withSkipOnFirstFailedRule(false)
				.withSkipOnFirstAppliedRule(false).build();
		for (final BeverageRule rule : rules)
		{
			rulesEngine.registerRule(rule);
		}

		// fire
		rulesEngine.fireRules();

		// collect results
		final List<Product> products = new ArrayList<>();
		for (final BeverageRule rule : rules)
		{
			if (rule.getProduct() != null)
			{
				products.add(rule.getProduct());
			}
		}

		return products;
	}

}

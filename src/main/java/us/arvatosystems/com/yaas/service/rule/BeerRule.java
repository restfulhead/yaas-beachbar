package us.arvatosystems.com.yaas.service.rule;

import us.arvatosystems.com.yaas.domain.Product;

public class BeerRule extends BeverageRule
{
	private static final Product beer = new Product("Beer", "123");

	public BeerRule(final String input)
	{
		super("beer rule", input, "beer");
	}

	@Override
	public void execute() throws Exception
	{
		this.product = beer;

	}

}

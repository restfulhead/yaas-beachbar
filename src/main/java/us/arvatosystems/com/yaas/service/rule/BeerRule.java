package us.arvatosystems.com.yaas.service.rule;

import us.arvatosystems.com.yaas.domain.Product;

public class BeerRule extends BeverageRule
{
	public static final Product BEER = new Product("Beer", "beer", 4.50, "56b12cccef2ed519dbf59d2f");

	public BeerRule(final String input)
	{
		super("beer rule", input, "beer", "🍺", "🍻");
	}

	@Override
	public void execute() throws Exception
	{
		this.product = BEER;
	}

}

package us.arvatosystems.com.yaas.service.rule;

import us.arvatosystems.com.yaas.domain.Product;

public class WaterRule extends BeverageRule
{
	public static final Product WATER = new Product("Water", "water", 2, "56b130191a6e854490d92a8a");

	public WaterRule(final String input)
	{
		super("water rule", input, "water");
	}

	@Override
	public void execute() throws Exception
	{
		this.product = WATER;

	}

}

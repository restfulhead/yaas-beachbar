package us.arvatosystems.com.yaas.service.rule;

import us.arvatosystems.com.yaas.domain.Product;

public class WaterRule extends BeverageRule
{
	private static final Product water = new Product("Water", "water", 2);

	public WaterRule(final String input)
	{
		super("water rule", input, "water");
	}

	@Override
	public void execute() throws Exception
	{
		this.product = water;

	}

}

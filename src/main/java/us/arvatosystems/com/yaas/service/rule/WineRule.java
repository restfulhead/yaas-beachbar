package us.arvatosystems.com.yaas.service.rule;

import us.arvatosystems.com.yaas.domain.Product;

public class WineRule extends BeverageRule
{
	private static final Product beer = new Product("Wine", "wine", 5.00);

	public WineRule(final String input)
	{
		super("wine rule", input, "wine", "vine", "🍷");
	}

	@Override
	public void execute() throws Exception
	{
		this.product = beer;
	}

}

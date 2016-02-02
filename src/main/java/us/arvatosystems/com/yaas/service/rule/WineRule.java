package us.arvatosystems.com.yaas.service.rule;

import us.arvatosystems.com.yaas.domain.Product;

public class WineRule extends BeverageRule
{
	public static final Product WINE = new Product("Wine", "wine", 5.00, "56b1317aef2ed519dbf59d33");

	public WineRule(final String input)
	{
		super("wine rule", input, "wine", "vine", "🍷");
	}

	@Override
	public void execute() throws Exception
	{
		this.product = WINE;
	}

}

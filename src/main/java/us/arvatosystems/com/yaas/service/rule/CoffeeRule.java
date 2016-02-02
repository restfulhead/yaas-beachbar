package us.arvatosystems.com.yaas.service.rule;

import us.arvatosystems.com.yaas.domain.Product;

public class CoffeeRule extends BeverageRule
{
	public static final Product COFFEE = new Product("Coffee", "coffee", 2.50, "56b13116e70eb321530bec16");

	public CoffeeRule(final String input)
	{
		super("coffee rule", input, "coffee", "cofe", "coffe", "cofee", "☕");
	}

	@Override
	public void execute() throws Exception
	{
		this.product = COFFEE;
	}

}

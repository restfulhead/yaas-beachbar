package us.arvatosystems.com.yaas.service.rule;

import us.arvatosystems.com.yaas.domain.Product;

public class CoffeeRule extends BeverageRule
{
	private static final Product coffee = new Product("Coffee", "coffee", 2.50);

	public CoffeeRule(final String input)
	{
		super("coffee rule", input, "coffee", "cofe", "coffe", "cofee", "☕");
	}

	@Override
	public void execute() throws Exception
	{
		this.product = coffee;
	}

}

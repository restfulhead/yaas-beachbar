package us.arvatosystems.com.yaas.service.rule;

import us.arvatosystems.com.yaas.domain.Product;

public class CocktailRule extends BeverageRule
{
	private static final Product cocktail = new Product("Cocktail", "cocktail", 8.00);

	public CocktailRule(final String input)
	{
		super("cocktail rule", input, "cocktail", "🍸", "🍹");
	}

	@Override
	public void execute() throws Exception
	{
		this.product = cocktail;
	}
}

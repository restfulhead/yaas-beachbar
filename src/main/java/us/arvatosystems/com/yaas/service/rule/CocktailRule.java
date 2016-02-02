package us.arvatosystems.com.yaas.service.rule;

import us.arvatosystems.com.yaas.domain.Product;

public class CocktailRule extends BeverageRule
{
	public static final Product COCKTAIL = new Product("Cocktail", "cocktail", 8.00, "56b130a54caee5fa7bc10515");

	public CocktailRule(final String input)
	{
		super("cocktail rule", input, "cocktail", "🍸", "🍹");
	}

	@Override
	public void execute() throws Exception
	{
		this.product = COCKTAIL;
	}
}

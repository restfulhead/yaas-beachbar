package us.arvatosystems.com.yaas.service.rule;

import java.util.Arrays;
import java.util.List;

import org.easyrules.core.BasicRule;

import us.arvatosystems.com.yaas.domain.Product;

public class BeverageRule extends BasicRule
{
	private final String input;
	private final List<String> words;

	protected Product product;

	public BeverageRule(final String name, final String input, final String... words)
	{
		super(name);
		this.input = input;
		this.words = Arrays.asList(words);
	}

	@Override
	public boolean evaluate()
	{
		for (final String word : words)
		{
			if (input.toLowerCase().contains(word.toLowerCase()))
			{
				return true;
			}
		}

		return false;
	}

	public String getInput()
	{
		return input;
	}

	public Product getProduct()
	{
		return product;
	}

}

package us.arvatosystems.com.yaas.domain;

public class Product
{
	private final String name;
	private final String sku;

	public Product(final String name, final String sku)
	{
		this.name = name;
		this.sku = sku;
	}

	public String getName()
	{
		return name;
	}

	public String getSku()
	{
		return sku;
	}

}

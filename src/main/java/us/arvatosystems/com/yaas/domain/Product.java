package us.arvatosystems.com.yaas.domain;

import java.math.BigDecimal;

public class Product
{
	private String name;
	private String sku;
	private BigDecimal price;

	public Product()
	{
		super();
	}

	public Product(final String name, final String sku, final double price)
	{
		this.name = name;
		this.sku = sku;
		this.price = BigDecimal.valueOf(price);
	}

	public String getName()
	{
		return name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public String getSku()
	{
		return sku;
	}

	public void setSku(final String sku)
	{
		this.sku = sku;
	}

	public BigDecimal getPrice()
	{
		return price;
	}

	public void setPrice(final BigDecimal price)
	{
		this.price = price;
	}

}

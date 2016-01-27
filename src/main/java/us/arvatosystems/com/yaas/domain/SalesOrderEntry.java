package us.arvatosystems.com.yaas.domain;

import java.math.BigDecimal;
import java.math.MathContext;

public class SalesOrderEntry
{
	private Product sku;
	private int amount;
	private BigDecimal unitPrice;
	private BigDecimal totalPrice;

	public SalesOrderEntry()
	{
		super();
	}

	public SalesOrderEntry(final Product sku, final int amount)
	{
		super();
		this.sku = sku;
		this.amount = amount;
		this.unitPrice = sku.getPrice();
		this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(amount), MathContext.DECIMAL64);
	}

	public Product getSku()
	{
		return sku;
	}

	public void setSku(final Product sku)
	{
		this.sku = sku;
	}

	public int getAmount()
	{
		return amount;
	}

	public void setAmount(final int amount)
	{
		this.amount = amount;
	}

	public BigDecimal getUnitPrice()
	{
		return unitPrice;
	}

	public void setUnitPrice(final BigDecimal unitPrice)
	{
		this.unitPrice = unitPrice;
	}

	public BigDecimal getTotalPrice()
	{
		return totalPrice;
	}

	public void setTotalPrice(final BigDecimal totalPrice)
	{
		this.totalPrice = totalPrice;
	}

}

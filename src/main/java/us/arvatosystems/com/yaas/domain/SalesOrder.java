package us.arvatosystems.com.yaas.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class SalesOrder
{
	private List<SalesOrderEntry> entries;

	private BigDecimal totalPrice;

	public SalesOrder()
	{
		super();
	}

	public SalesOrder(final List<SalesOrderEntry> entries)
	{
		this.entries = entries;

		BigDecimal total = BigDecimal.ZERO;
		for (final SalesOrderEntry entry : entries)
		{
			total = total.add(entry.getTotalPrice());
		}

		this.totalPrice = total.setScale(2, RoundingMode.HALF_UP);
	}

	public List<SalesOrderEntry> getEntries()
	{
		return entries;
	}

	public void setEntries(final List<SalesOrderEntry> entries)
	{
		this.entries = entries;
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

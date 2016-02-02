package us.arvatosystems.com.yaas.domain;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class Product
{
	private String name;
	private String description;
	private String sku;
	private BigDecimal price;
	private String link;
	private List<Media> images;

	public Product()
	{
		super();
	}

	public Product(final String name, final String sku, final double price, final String mediaId)
	{
		this.name = name;
		this.description = name;
		this.sku = sku;
		this.price = BigDecimal.valueOf(price);
		this.images = Collections.singletonList(new Media(mediaId));
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

	public String getDescription()
	{
		return description;
	}

	public void setDescription(final String description)
	{
		this.description = description;
	}

	public String getLink()
	{
		return link;
	}

	public void setLink(final String link)
	{
		this.link = link;
	}

	public List<Media> getImages()
	{
		return images;
	}

	public void setImages(final List<Media> images)
	{
		this.images = images;
	}

}

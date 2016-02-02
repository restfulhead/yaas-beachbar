package us.arvatosystems.com.yaas.domain;

public class Media
{
	private static final String MEDIA_URL = "https://api.yaas.io/hybris/media/v1/beachbar/hybris.product/media/";

	private String id;
	private String url;

	public Media()
	{
		super();
	}

	public Media(final String id)
	{
		this(id, MEDIA_URL + id);
	}

	public Media(final String id, final String url)
	{
		super();
		this.id = id;
		this.url = url;
	}

	public String getId()
	{
		return id;
	}

	public void setId(final String id)
	{
		this.id = id;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(final String url)
	{
		this.url = url;
	}

}

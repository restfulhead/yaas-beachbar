package us.arvatosystems.com.yaas.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of a read operation on a topic.
 */
public class PubSubReadResponse
{

	private String id;
	private List<PubSubEvent> events = new ArrayList<>();

	/** encrypted token bearing info on events to be committed; will not be included in responses to auto committed read requests . **/
	private String token;

	public List<PubSubEvent> getEvents()
	{
		return events;
	}

	public void setEvents(final List<PubSubEvent> events)
	{
		this.events = events;
	}

	public String getId()
	{
		return id;
	}

	public void setId(final String id)
	{
		this.id = id;
	}

	public String getToken()
	{
		return token;
	}

	public void setToken(final String token)
	{
		this.token = token;
	}
}

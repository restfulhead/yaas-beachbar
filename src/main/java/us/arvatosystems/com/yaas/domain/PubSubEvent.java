package us.arvatosystems.com.yaas.domain;

import java.util.Map;

/**
 * Represents an Event from a Topic.
 */
public class PubSubEvent
{

	private String id;
	/** The number of milliseconds since EPOCH until date when event was received. **/
	private long createdAt;
	/** Tenant on behalf of which this event was published. **/
	private String tenant;
	/**
	 * Client that published the event, e.g. 'arvato.integrationtest' if publisher is the order service.
	 **/
	private String sourceClient;
	/** Type of this event. **/
	private String eventType;
	/** String containing event details; typically it's a JSON document string. **/
	private String payload;
	/** metadata describing the event. Might contain schema or version information . **/
	private Map<String, String> metadata;

	public long getCreatedAt()
	{
		return createdAt;
	}

	public void setCreatedAt(final long createdAt)
	{
		this.createdAt = createdAt;
	}

	public String getEventType()
	{
		return eventType;
	}

	public void setEventType(final String eventType)
	{
		this.eventType = eventType;
	}

	public String getId()
	{
		return id;
	}

	public void setId(final String id)
	{
		this.id = id;
	}

	public Map<String, String> getMetadata()
	{
		return metadata;
	}

	public void setMetadata(final Map<String, String> metadata)
	{
		this.metadata = metadata;
	}

	public String getPayload()
	{
		return payload;
	}

	public void setPayload(final String payload)
	{
		this.payload = payload;
	}

	public String getSourceClient()
	{
		return sourceClient;
	}

	public void setSourceClient(final String sourceClient)
	{
		this.sourceClient = sourceClient;
	}

	public String getTenant()
	{
		return tenant;
	}

	public void setTenant(final String tenant)
	{
		this.tenant = tenant;
	}
}

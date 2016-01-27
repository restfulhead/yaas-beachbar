package us.arvatosystems.com.yaas.domain;

/**
 * Optional settings given when reading messages on a topic.
 */
public class PubSubReadSettings
{
	/** Maximum number of events to retrieve for processing. **/
	private int numEvents;

	/**
	 * time to live (in milliseconds) before the read lock is expired. For explicit commits defaults to 300000ms (5min), for auto
	 * commit requests defaults to 5000ms (5sec)."
	 **/
	private int ttlMs;

	/** When set to true commit will be invoked automatically. **/
	private boolean autoCommit;

	public PubSubReadSettings()
	{
		super();
	}

	public PubSubReadSettings(final int numEvents, final int ttlMs, final boolean autoCommit)
	{
		super();
		this.numEvents = numEvents;
		this.ttlMs = ttlMs;
		this.autoCommit = autoCommit;
	}

	public boolean isAutoCommit()
	{
		return autoCommit;
	}

	public void setAutoCommit(final boolean autoCommit)
	{
		this.autoCommit = autoCommit;
	}

	public int getNumEvents()
	{
		return numEvents;
	}

	public void setNumEvents(final int numEvents)
	{
		this.numEvents = numEvents;
	}

	public int getTtlMs()
	{
		return ttlMs;
	}

	public void setTtlMs(final int ttlMs)
	{
		this.ttlMs = ttlMs;
	}
}

package us.arvatosystems.com.yaas.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import us.arvatosystems.com.yaas.service.message.IncomingMessageEvent;
import us.arvatosystems.com.yaas.service.message.OutgoingMessageEvent;

public class LoggingApplicationEventPublisher implements ApplicationEventPublisher
{
	static final Logger LOG = LoggerFactory.getLogger(LoggingApplicationEventPublisher.class);

	private final List<Object> events = new ArrayList<>();

	@Override
	public void publishEvent(final ApplicationEvent event)
	{
		LOG.info("Received event {}", event);
		events.add(event);
	}

	@Override
	public void publishEvent(final Object event)
	{
		LOG.info("Received event {}", event);
		events.add(events);
	}

	public List<Object> getEvents()
	{
		return events;
	}

	public OutgoingMessageEvent getLatestOutgoingMessageEvent()
	{
		return (OutgoingMessageEvent) getLatestEvent();
	}

	public IncomingMessageEvent getLatestIncomingMessageEvent()
	{
		return (IncomingMessageEvent) getLatestEvent();
	}

	public Object getLatestEvent()
	{
		if (events.size() < 1)
		{
			return null;
		}

		return events.get(events.size() - 1);
	}
}
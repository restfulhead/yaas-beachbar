package us.arvatosystems.com.yaas.service.message;

import org.springframework.context.ApplicationEvent;

import us.arvatosystems.com.yaas.domain.SMSMessage;

public class IncomingMessageEvent extends ApplicationEvent
{
	private static final long serialVersionUID = -8387540812853276845L;

	private final SMSMessage message;

	public IncomingMessageEvent(final Object source, final String from, final String to, final String message)
	{
		super(source);

		this.message = new SMSMessage(from, to, message);
	}

	public SMSMessage getMessage()
	{
		return message;
	}

}

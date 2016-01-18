package us.arvatosystems.com.yaas.domain;


public class SMSMessage
{
	private String fromNumber;
	private String toNumber;
	private String messageText;

	public SMSMessage()
	{
		super();
	}

	public SMSMessage(final String from, final String to, final String message)
	{
		super();
		this.fromNumber = from;
		this.toNumber = to;
		this.messageText = message;
	}

	public String getFromNumber()
	{
		return fromNumber;
	}

	public void setFromNumber(final String from)
	{
		this.fromNumber = from;
	}

	public String getToNumber()
	{
		return toNumber;
	}

	public void setToNumber(final String to)
	{
		this.toNumber = to;
	}

	public String getMessageText()
	{
		return messageText;
	}

	public void setMessageText(final String message)
	{
		this.messageText = message;
	}


}

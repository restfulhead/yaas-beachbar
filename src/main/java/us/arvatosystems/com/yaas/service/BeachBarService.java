package us.arvatosystems.com.yaas.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import us.arvatosystems.com.yaas.domain.Product;
import us.arvatosystems.com.yaas.service.message.IncomingMessageEvent;
import us.arvatosystems.com.yaas.service.message.OutgoingMessageEvent;
import us.arvatosystems.com.yaas.service.rule.RulesEngineService;

@Component
public class BeachBarService implements ApplicationListener<IncomingMessageEvent>
{
	@Autowired
	private RulesEngineService rulesEngine;

	@Autowired
	private ApplicationEventPublisher publisher;

	@Override
	public void onApplicationEvent(final IncomingMessageEvent event)
	{
		// todo check finite state machine by number to decide how to process message

	}

	protected void doWelcome(final IncomingMessageEvent event)
	{
		final String reply = "Welcome to the beach bar! How can I help you?";
		publisher.publishEvent(new OutgoingMessageEvent(this, getFrom(), event.getMessage().getFromNumber(), reply));
	}

	protected void doIdentifyOrder(final IncomingMessageEvent event)
	{
		final List<Product> items = rulesEngine.identifyBeverages(event.getMessage().getMessageText());

		if (items.size() > 0)
		{
			// add items to shopping cart or alternatively to state engine
		}
		else
		{
			final String reply = "Sorry, I couldn't understand your order.";
			publisher.publishEvent(new OutgoingMessageEvent(this, getFrom(), event.getMessage().getFromNumber(), reply));
		}

	}

	//protected void doPlaceOrder(final String replyTo)


	protected String getFrom()
	{
		return "TODO";
	}

}

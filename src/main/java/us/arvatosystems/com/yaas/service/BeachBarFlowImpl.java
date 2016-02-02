package us.arvatosystems.com.yaas.service;
import static au.com.ds.ef.FlowBuilder.from;
import static au.com.ds.ef.FlowBuilder.on;

import java.util.ArrayList;
import java.util.List;

import jersey.repackaged.com.google.common.base.Throwables;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import us.arvatosystems.com.yaas.domain.Product;
import us.arvatosystems.com.yaas.service.message.IncomingMessageEvent;
import us.arvatosystems.com.yaas.service.message.OutgoingMessageEvent;
import us.arvatosystems.com.yaas.service.rule.RulesEngineService;
import au.com.ds.ef.EasyFlow;
import au.com.ds.ef.EventEnum;
import au.com.ds.ef.StateEnum;
import au.com.ds.ef.StatefulContext;
import au.com.ds.ef.call.ContextHandler;
import au.com.ds.ef.call.StateHandler;
import au.com.ds.ef.err.LogicViolationError;

/**
 * A simple state machine that manages the interaction with a customer.
 */
@Component
public class BeachBarFlowImpl implements InitializingBean, BeachBarFlow
{
	protected enum States implements StateEnum
	{
		WELCOME, WAITING_FOR_ORDER, PARSE_ORDER, WAITING_FOR_ORDER_CONFIRMATION, PARSE_ORDER_CONFIRMATION, COMPLETE;
	}

	protected enum Events implements EventEnum
	{
		welcomeMessageSent, incomingMessageReceived, orderSummarySent, incomingMessageNotUnderstood, interactionComplete;
	}

	@Autowired
	private ApplicationEventPublisher publisher;

	@Autowired
	private RulesEngineService rulesEngine;

	private Callback callback;

	private EasyFlow<Conversation> flow;


	@Override
	public void afterPropertiesSet() throws Exception
	{
		this.flow = from(States.WELCOME)
				.transit(on(Events.welcomeMessageSent).to(States.WAITING_FOR_ORDER)
						.transit(on(Events.incomingMessageReceived).to(States.PARSE_ORDER)
								.transit(on(Events.orderSummarySent).to(States.WAITING_FOR_ORDER_CONFIRMATION)
										.transit(on(Events.incomingMessageReceived).to(States.PARSE_ORDER_CONFIRMATION)
												.transit(on(Events.interactionComplete).finish(States.COMPLETE),
															on(Events.incomingMessageNotUnderstood).to(States.WAITING_FOR_ORDER_CONFIRMATION))),
										on(Events.incomingMessageNotUnderstood).to(States.WAITING_FOR_ORDER)
			     )))
				.whenEnter(States.WELCOME, new ContextHandler<Conversation>()
				{
					@Override
					public void call(final Conversation ctx) throws Exception
					{
						sendWelcomeMessage(ctx);
						ctx.trigger(Events.welcomeMessageSent);
					}
				}).whenEnter(States.PARSE_ORDER, new ContextHandler<Conversation>()
				{
					@Override
					public void call(final Conversation ctx) throws Exception
					{
						if (parseOrderAndSendSummary(ctx)) {
							ctx.trigger(Events.orderSummarySent);
						} else {
							ctx.trigger(Events.incomingMessageNotUnderstood);
						}
					}
				}).whenEnter(States.PARSE_ORDER_CONFIRMATION, new ContextHandler<Conversation>()
				{
					@Override
					public void call(final Conversation ctx) throws Exception
					{
						if (placeOrCancelOrderAndSendFinalMessage(ctx))
						{
							ctx.trigger(Events.interactionComplete);
						}
						else
						{
							ctx.trigger(Events.incomingMessageNotUnderstood);
						}
					}
				}).whenFinalState(new StateHandler<Conversation>()
				{
					@Override
					public void call(final StateEnum state, final Conversation ctx) throws Exception
					{
						if (callback != null)
						{
							callback.onComplete(ctx);
						}
					}
				});
	}

	@Override
	public Conversation start(final IncomingMessageEvent event)
	{
		final Conversation ctx = new Conversation(event.getMessage().getFromNumber(), event.getMessage().getToNumber(), event
				.getMessage().getMessageText());
		flow.start(true, ctx);
		return ctx;
	}

	@Override
	public void proceed(final Conversation ctx, final IncomingMessageEvent event)
	{
		ctx.setLastMessageFromCustomer(event.getMessage().getMessageText());
		try
		{
			ctx.trigger(Events.incomingMessageReceived);
		}
		catch (final LogicViolationError e)
		{
			throw Throwables.propagate(e);
		}
	}

	protected void sendWelcomeMessage(final Conversation ctx)
	{
		final String reply = "Welcome to the beach bar! How can I help you?";
		publisher.publishEvent(new OutgoingMessageEvent(this, ctx.beachBarNo, ctx.customerNo, reply));
	}

	protected boolean parseOrderAndSendSummary(final Conversation ctx)
	{
		final List<Product> items = rulesEngine.identifyBeverages(ctx.getLastMessageFromCustomer());

		if (items.size() > 0)
		{
			// remember identified products
			// perhaps in future we could add them to the YaaS shopping cart
			ctx.getCurrentOrder().addAll(items);

			final StringBuilder sb = new StringBuilder("Confirm your order of ");
			for (final Product product : items)
			{
				sb.append("1 " + product.getName()).append(", ");
			}

			sendMessage(ctx, sb.substring(0, sb.length() - 2) + ". Reply YES or NO");
			return true;
		}

		sendMessage(ctx, "Sorry, I couldn't understand your order. Please try again!");
		return false;
	}

	protected boolean placeOrCancelOrderAndSendFinalMessage(final Conversation ctx)
	{
		final String msg = ctx.getLastMessageFromCustomer().toLowerCase();
		if (msg.startsWith("yes") || msg.contains("👍"))
		{
			// ok, we're good to go with placing the order
			placeOrder(ctx);
			sendMessage(ctx, "Thank you! Your order is in the works.");
			return true;
		}
		else if (msg.startsWith("no"))
		{
			// nope, customer doesn't want products
			ctx.getCurrentOrder().clear();
			sendMessage(ctx, "Ok, we canceled your order.");
			return true;
		}

		sendMessage(ctx, "Please reply with YES or NO");
		return false;
	}

	protected void placeOrder(final Conversation ctx)
	{
		// TODO
	}

	protected void sendMessage(final Conversation ctx, final String message)
	{
		publisher.publishEvent(new OutgoingMessageEvent(this, ctx.beachBarNo, ctx.customerNo, message));
	}

	public void setPublisher(final ApplicationEventPublisher publisher)
	{
		this.publisher = publisher;
	}

	public void setRulesEngine(final RulesEngineService rulesEngine)
	{
		this.rulesEngine = rulesEngine;
	}

	@Override
	public void setCallback(final Callback callback)
	{
		this.callback = callback;
	}

	public static class Conversation extends StatefulContext
	{
		private static final long serialVersionUID = 6078470370889712596L;

		private final String customerNo;
		private final String beachBarNo;

		private String lastMessageFromCustomer;

		private final List<Product> currentOrder = new ArrayList<>();

		public Conversation(final String customerNo, final String beachBarNo, final String lastMessage)
		{
			super(customerNo);
			this.customerNo = customerNo;
			this.beachBarNo = beachBarNo;
			this.lastMessageFromCustomer = lastMessage;
		}

		public String getCustomerNo()
		{
			return customerNo;
		}

		public String getBeachBarNo()
		{
			return beachBarNo;
		}

		public String getLastMessageFromCustomer()
		{
			return lastMessageFromCustomer;
		}

		public void setLastMessageFromCustomer(final String lastMessage)
		{
			this.lastMessageFromCustomer = lastMessage;
		}

		public List<Product> getCurrentOrder()
		{
			return currentOrder;
		}
	}


}

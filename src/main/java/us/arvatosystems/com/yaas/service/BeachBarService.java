package us.arvatosystems.com.yaas.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import us.arvatosystems.com.yaas.Util;
import us.arvatosystems.com.yaas.service.BeachBarFlow.Callback;
import us.arvatosystems.com.yaas.service.BeachBarFlowImpl.Conversation;
import us.arvatosystems.com.yaas.service.message.IncomingMessageEvent;

@Component
public class BeachBarService implements ApplicationListener<IncomingMessageEvent>, InitializingBean
{
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(BeachBarService.class);

	@Autowired
	private BeachBarFlow flow;

	/**
	 * Holds the state for each customer (phone number) in memory.
	 */
	private final Map<String, Conversation> state = new HashMap<>();

	@Override
	public void afterPropertiesSet() throws Exception
	{
		this.flow.setCallback(new Callback()
		{
			@Override
			public void onComplete(final Conversation conversation)
			{
				// remove state after completion
				state.remove(conversation.getCustomerNo());
			}
		});
	}

	@Override
	public void onApplicationEvent(final IncomingMessageEvent event)
	{
		final String customer = event.getMessage().getFromNumber();

		if (state.containsKey(customer))
		{
			// customer already communicated to us
			Conversation ctx = state.get(customer);

			// ok, last interaction was complete. let's remove and start a new one.
			if (ctx.isTerminated()) {
				LOG.info("Previous conversation with customer {} is complete. Starting new one...", Util.maskPhoneNo(customer));
				ctx = null;
				state.put(customer, flow.start(event));
			}
			else
			{
				// continue previous interaction
				LOG.info("Proceed with existing conversation with customer {}.", Util.maskPhoneNo(customer));
				flow.proceed(ctx, event);
			}
		}
		else
		{
			// new customer communication -> start a new flow
			LOG.info("Start a new conversation with customer {}.", Util.maskPhoneNo(customer));
			state.put(customer, flow.start(event));
		}
	}

	protected Conversation createNewContext(final IncomingMessageEvent event)
	{
		return new Conversation(event.getMessage().getFromNumber(), event.getMessage().getToNumber(), event.getMessage()
				.getMessageText());
	}

	public void setFlow(final BeachBarFlow flow)
	{
		this.flow = flow;
	}

	protected Map<String, Conversation> getState()
	{
		return state;
	}

}

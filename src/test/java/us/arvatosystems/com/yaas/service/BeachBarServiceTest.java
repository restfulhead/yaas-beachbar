package us.arvatosystems.com.yaas.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import us.arvatosystems.com.yaas.service.BeachBarFlowImpl.Conversation;
import us.arvatosystems.com.yaas.service.message.IncomingMessageEvent;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/applicationContext.xml" })
public class BeachBarServiceTest
{
	private static final Logger LOG = LoggerFactory.getLogger(BeachBarServiceTest.class);

	private BeachBarService service;
	private LoggingFlow flow;

	@Before
	public void setup() throws Exception
	{
		MockitoAnnotations.initMocks(this);

		flow = new LoggingFlow();
		service = new BeachBarService();
		service.setFlow(flow);
		service.afterPropertiesSet();
	}

	@Test
	public void shouldHandleMultipleInteractions()
	{
		// start conversation with first customer
		final String customerA = "a";
		service.onApplicationEvent(createMessage(customerA, "hi"));
		assertThat(flow.startEvents.size(), is(1));
		assertThat(flow.conversations.size(), is(1));
		assertThat(service.getState().size(), is(1));
		assertThat(service.getState().get(customerA).getCustomerNo(), equalTo(customerA));

		// start conversation with another customer
		final String customerB = "b";
		service.onApplicationEvent(createMessage(customerB, "hallo"));
		assertThat(flow.startEvents.size(), is(2));
		assertThat(flow.conversations.size(), is(2));
		assertThat(service.getState().size(), is(2));
		assertThat(service.getState().get(customerA).getCustomerNo(), equalTo(customerA));
		assertThat(service.getState().get(customerB).getCustomerNo(), equalTo(customerB));

		// continue with customer A
		service.onApplicationEvent(createMessage(customerA, "how are you?"));
		assertThat(flow.startEvents.size(), is(2));
		assertThat(flow.conversations.size(), is(2));
		assertThat(service.getState().size(), is(2));
		assertThat(service.getState().get(customerA).getCustomerNo(), equalTo(customerA));
		assertThat(service.getState().get(customerB).getCustomerNo(), equalTo(customerB));
		assertThat(flow.proceedEvents.size(), is(1));

		// continue with customer B
		service.onApplicationEvent(createMessage(customerB, "Wazup?"));
		assertThat(flow.startEvents.size(), is(2));
		assertThat(flow.conversations.size(), is(2));
		assertThat(service.getState().size(), is(2));
		assertThat(service.getState().get(customerA).getCustomerNo(), equalTo(customerA));
		assertThat(service.getState().get(customerB).getCustomerNo(), equalTo(customerB));
		assertThat(flow.proceedEvents.size(), is(2));

		// start another conversation
		final String customerC = "c";
		service.onApplicationEvent(createMessage(customerC, "Wazup?"));
		assertThat(flow.startEvents.size(), is(3));
		assertThat(flow.conversations.size(), is(3));
		assertThat(service.getState().size(), is(3));
		assertThat(service.getState().get(customerA).getCustomerNo(), equalTo(customerA));
		assertThat(service.getState().get(customerB).getCustomerNo(), equalTo(customerB));
		assertThat(service.getState().get(customerC).getCustomerNo(), equalTo(customerC));
		assertThat(flow.proceedEvents.size(), is(2));

		// flag customer a as complete
		flow.getConversations().get(0).setTerminated(true);
		// this should result in a new conversation
		service.onApplicationEvent(createMessage(customerA, "I'm back!"));
		assertThat(flow.startEvents.size(), is(4));
		assertThat(flow.conversations.size(), is(4));
		assertThat(service.getState().size(), is(3));
		assertThat(service.getState().get(customerA).getCustomerNo(), equalTo(customerA));
		assertThat(service.getState().get(customerB).getCustomerNo(), equalTo(customerB));
		assertThat(service.getState().get(customerC).getCustomerNo(), equalTo(customerC));
		assertThat(flow.proceedEvents.size(), is(2));

		// simulate complete event for customer b
		flow.complete(service.getState().get(customerB));
		assertThat(service.getState().size(), is(2));
		assertThat(service.getState().get(customerA).getCustomerNo(), equalTo(customerA));
		assertNull(service.getState().get(customerB));
		assertThat(service.getState().get(customerC).getCustomerNo(), equalTo(customerC));

		// start a new conversation with customer b
		service.onApplicationEvent(createMessage(customerB, "I'm back!"));
		assertThat(flow.startEvents.size(), is(5));
		assertThat(flow.conversations.size(), is(5));
		assertThat(service.getState().size(), is(3));
		assertThat(service.getState().get(customerA).getCustomerNo(), equalTo(customerA));
		assertThat(service.getState().get(customerB).getCustomerNo(), equalTo(customerB));
		assertThat(service.getState().get(customerC).getCustomerNo(), equalTo(customerC));
		assertThat(flow.proceedEvents.size(), is(2));
	}

	public IncomingMessageEvent createMessage(final String customer, final String message)
	{
		return new IncomingMessageEvent(this, customer, "me", message);
	}

	protected static class LoggingFlow implements BeachBarFlow
	{
		private final List<MutableConversation> conversations = new ArrayList<>();
		private final List<IncomingMessageEvent> startEvents = new ArrayList<>();
		private final List<IncomingMessageEvent> proceedEvents = new ArrayList<>();

		private Callback callback;

		@Override
		public Conversation start(final IncomingMessageEvent event)
		{
			LOG.info("Received event {}", event);
			startEvents.add(event);
			final MutableConversation newConversation = new MutableConversation(event.getMessage().getFromNumber(), event
					.getMessage()
					.getToNumber(), event.getMessage()
					.getFromNumber());
			conversations.add(newConversation);
			return newConversation;
		}

		@Override
		public void proceed(final Conversation ctx, final IncomingMessageEvent event)
		{
			LOG.info("Received event {}", event);
			proceedEvents.add(event);
		}

		public void complete(final Conversation conversation)
		{
			if (callback != null)
			{
				callback.onComplete(conversation);
			}
		}

		public List<MutableConversation> getConversations()
		{
			return conversations;
		}

		public List<IncomingMessageEvent> getStartEvents()
		{
			return startEvents;
		}

		public List<IncomingMessageEvent> getProceedEvents()
		{
			return proceedEvents;
		}

		@Override
		public void setCallback(final Callback callback)
		{
			this.callback = callback;
		}
	}

	protected static class MutableConversation extends Conversation
	{
		private static final long serialVersionUID = -8758039497793339881L;

		public MutableConversation(final String customerNo, final String beachBarNo, final String lastMessage)
		{
			super(customerNo, beachBarNo, lastMessage);
		}

		private boolean terminated;

		@Override
		public boolean isTerminated()
		{
			return this.terminated;
		}

		public void setTerminated(final boolean terminated)
		{
			this.terminated = terminated;
		}

	}
}

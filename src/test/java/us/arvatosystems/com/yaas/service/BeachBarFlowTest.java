package us.arvatosystems.com.yaas.service;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import us.arvatosystems.com.yaas.domain.Product;
import us.arvatosystems.com.yaas.service.BeachBarFlowImpl.Conversation;
import us.arvatosystems.com.yaas.service.BeachBarFlowImpl.States;
import us.arvatosystems.com.yaas.service.checkout.OrderService;
import us.arvatosystems.com.yaas.service.message.IncomingMessageEvent;
import us.arvatosystems.com.yaas.service.rule.RulesEngineService;
import au.com.ds.ef.StateEnum;
import au.com.ds.ef.StatefulContext;
import au.com.ds.ef.err.LogicViolationError;

import com.google.common.base.Throwables;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/applicationContext.xml" })
public class BeachBarFlowTest
{
	private static final Logger LOG = LoggerFactory.getLogger(BeachBarFlowTest.class);

	@Mock
	private RulesEngineService rulesEngine;

	@Mock
	private BeachBarFlow.Callback callback;

	@Mock
	private OrderService orderService;

	private BeachBarFlowImpl flow;
	private LoggingApplicationEventPublisher publisher;

	@Before
	public void setup() throws Exception
	{
		MockitoAnnotations.initMocks(this);

		publisher = new LoggingApplicationEventPublisher();

		when(rulesEngine.identifyBeverages("I want a beer!")).thenReturn(
				Collections.singletonList(new Product("Beer", "beer", 4.50, "")));
		when(rulesEngine.identifyBeverages("I want a water!")).thenReturn(
				Collections.singletonList(new Product("Water", "water", 2, "")));

		doNothing().when(orderService).placeOrder(anyList(), anyString());

		flow = new BeachBarFlowImpl();
		flow.setPublisher(publisher);
		flow.setRulesEngine(rulesEngine);
		flow.setCallback(callback);
		flow.setOrderService(orderService);

		flow.afterPropertiesSet();
	}

	@Test
	public void shouldCompleteOrder() throws LogicViolationError
	{
		final Conversation ctx = testWelcome();

		// customer sends order
		flow.proceed(ctx, createIncomingEvent(ctx, "I want a beer!"));

		// beach bar parses order and sends order summary
		waitForState(ctx, States.WAITING_FOR_ORDER_CONFIRMATION);
		assertThat(publisher.getEvents().size(), is(2));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getFromNumber(), equalTo("456"));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getToNumber(), equalTo("123"));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getMessageText(), containsString("1 Beer. Reply YES"));

		// customer confirms
		flow.proceed(ctx, createIncomingEvent(ctx, "YES"));
		waitForState(ctx, States.COMPLETE);
		assertThat(publisher.getEvents().size(), is(3));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getFromNumber(), equalTo("456"));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getToNumber(), equalTo("123"));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getMessageText(), containsString("Thank you!"));

		assertTrue(ctx.isTerminated());
		verify(callback, times(1)).onComplete(any());
		verify(orderService, times(1)).placeOrder(anyList(), anyString());
	}

	@Test
	public void shouldRetryAndThenCancelOrder() throws LogicViolationError
	{
		final Conversation ctx = testWelcome();

		// customer sends order
		flow.proceed(ctx, createIncomingEvent(ctx, "I want a water!"));

		// beach bar parses order and sends order summary
		waitForState(ctx, States.WAITING_FOR_ORDER_CONFIRMATION);
		assertThat(publisher.getEvents().size(), is(2));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getFromNumber(), equalTo("456"));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getToNumber(), equalTo("123"));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getMessageText(), containsString("1 Water. Reply YES"));
		assertFalse(ctx.isTerminated());

		// customer replies with unexpected message
		flow.proceed(ctx, createIncomingEvent(ctx, "Wzzup??!"));
		waitForMessageCount(publisher.getEvents(), 3);
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getFromNumber(), equalTo("456"));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getToNumber(), equalTo("123"));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getMessageText(), containsString("YES or NO"));
		assertFalse(ctx.isTerminated());

		// customer cancels
		flow.proceed(ctx, createIncomingEvent(ctx, "No!!"));

		waitForState(ctx, States.COMPLETE);
		assertThat(publisher.getEvents().size(), is(4));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getFromNumber(), equalTo("456"));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getToNumber(), equalTo("123"));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getMessageText(), containsString("canceled"));

		assertTrue(ctx.isTerminated());
		verify(callback, times(1)).onComplete(any());
		verify(orderService, times(0)).placeOrder(anyList(), anyString());
	}

	@Test
	public void shouldTryAgainOfOrderNotUnderstood() throws LogicViolationError
	{
		final Conversation ctx = testWelcome();

		// customer sends order
		flow.proceed(ctx, createIncomingEvent(ctx, "I want a million dollar!"));

		// beach bar parses order and replies with a "try again" message
		waitForMessageCount(publisher.getEvents(), 2);
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getFromNumber(), equalTo("456"));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getToNumber(), equalTo("123"));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getMessageText(), containsString("try again"));

		// now send a valid order
		flow.proceed(ctx, createIncomingEvent(ctx, "I want a beer!"));
		waitForState(ctx, States.WAITING_FOR_ORDER_CONFIRMATION);
		assertThat(publisher.getEvents().size(), is(3));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getFromNumber(), equalTo("456"));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getToNumber(), equalTo("123"));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getMessageText(), containsString("order"));

		assertFalse(ctx.isTerminated());
		verify(callback, times(0)).onComplete(any());
		verify(orderService, times(0)).placeOrder(anyList(), anyString());
	}

	@Test
	public void shouldRetryIfOrderPostFailed() throws LogicViolationError
	{
		doThrow(new IllegalStateException("Simulating order POST failure")).when(orderService).placeOrder(anyList(), anyString());

		final Conversation ctx = testWelcome();

		// customer sends order
		flow.proceed(ctx, createIncomingEvent(ctx, "I want a water!"));

		// beach bar parses order and sends order summary
		waitForState(ctx, States.WAITING_FOR_ORDER_CONFIRMATION);
		assertThat(publisher.getEvents().size(), is(2));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getFromNumber(), equalTo("456"));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getToNumber(), equalTo("123"));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getMessageText(), containsString("1 Water. Reply YES"));
		assertFalse(ctx.isTerminated());

		// customer replies with unexpected message
		flow.proceed(ctx, createIncomingEvent(ctx, "Wzzup??!"));
		waitForMessageCount(publisher.getEvents(), 3);
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getFromNumber(), equalTo("456"));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getToNumber(), equalTo("123"));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getMessageText(), containsString("YES or NO"));
		assertFalse(ctx.isTerminated());

		// customer confirms, but placing the order fails
		flow.proceed(ctx, createIncomingEvent(ctx, "👍"));

		waitForMessageCount(publisher.getEvents(), 4);
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getFromNumber(), equalTo("456"));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getToNumber(), equalTo("123"));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getMessageText(), containsString("try again"));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getMessageText(), containsString("can't process"));

		// now simulate success
		doNothing().when(orderService).placeOrder(anyList(), anyString());

		flow.proceed(ctx, createIncomingEvent(ctx, "👍"));
		waitForState(ctx, States.COMPLETE);
		assertThat(publisher.getEvents().size(), is(5));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getMessageText(), containsString("Thank you!"));

		assertTrue(ctx.isTerminated());
		verify(callback, times(1)).onComplete(any());
		verify(orderService, times(2)).placeOrder(anyList(), anyString());
	}

	protected Conversation testWelcome()
	{
		// customer says hi
		final Conversation ctx = flow.start(new IncomingMessageEvent(this, "123", "456", "Hi!"));

		// beach bar replies with welcome
		waitForState(ctx, States.WAITING_FOR_ORDER);
		assertThat(publisher.getEvents().size(), is(1));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getFromNumber(), equalTo("456"));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getToNumber(), equalTo("123"));
		assertThat(publisher.getLatestOutgoingMessageEvent().getMessage().getMessageText(), containsString("Welcome"));

		return ctx;
	}

	protected IncomingMessageEvent createIncomingEvent(final Conversation ctx, final String message)
	{
		return new IncomingMessageEvent(this, ctx.getCustomerNo(), ctx.getBeachBarNo(), message);
	}

	protected void waitForState(final StatefulContext ctx, final StateEnum state)
	{
		int waitTime = 0;
		while (!state.equals(ctx.getState()))
		{
			LOG.info("Waiting for context '{}' to enter state '{}'", ctx, state);
			try
			{
				Thread.sleep(1000);
			}
			catch (final InterruptedException e)
			{
				Throwables.propagate(e);
			}
			waitTime += 1000;

			if (waitTime > 10000)
			{
				throw new IllegalStateException("Giving up on context '" + ctx + "' to enter state '" + state + "'");
			}
		}
	}

	protected void waitForMessageCount(final List<?> list, final int count)
	{
		int waitTime = 0;
		while (list.size() < count)
		{
			LOG.info("Waiting for list count of '{}', current count '{}'", count, list.size());
			try
			{
				Thread.sleep(1000);
			}
			catch (final InterruptedException e)
			{
				Throwables.propagate(e);
			}
			waitTime += 1000;

			if (waitTime > 10000)
			{
				throw new IllegalStateException("Giving up on context '{}' to enter state '{}'");
			}
		}
	}
}

package us.arvatosystems.com.yaas.service.message;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Link.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import us.arvatosystems.com.yaas.domain.PubSubEvent;
import us.arvatosystems.com.yaas.domain.PubSubReadResponse;
import us.arvatosystems.com.yaas.service.LoggingApplicationEventPublisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.cloud.yaas.api.HybrisPubSubServiceApiClient;
import com.sap.cloud.yaas.servicesdk.authorization.integration.AuthorizedExecutionTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/applicationContext.xml" })
public class IncomingMessagePollerTest
{
	private IncomingMessagePoller poller;

	@Mock
	private AuthorizedExecutionTemplate authTemplate;

	@Mock
	private HybrisPubSubServiceApiClient pubSubClient;

	@Autowired
	private ObjectMapper objectMapper;

	private LoggingApplicationEventPublisher testPublisher;

	@Before
	public void setup()
	{
		MockitoAnnotations.initMocks(this);

		final PubSubReadResponse mockResponse = new PubSubReadResponse();
		final PubSubEvent mockEvent = new PubSubEvent();
		mockEvent
				.setPayload("{\"fromNumber\":\"+1231231234\",\"toNumber\":\"+6543219876\",\"messageText\":\"Hi there! 🍺 and 🍻\"}");
		mockResponse.setEvents(Collections.singletonList(mockEvent));

		testPublisher = new LoggingApplicationEventPublisher();

		when(authTemplate.executeAuthorized(any(), any(), any())).thenReturn(new MockResponse(200, mockResponse));

		this.poller = new IncomingMessagePoller();
		poller.setAuthTemplate(authTemplate);
		poller.setObjectMapper(objectMapper);
		poller.setPublisher(testPublisher);
	}

	@Test
	public void shouldReadMessage()
	{
		poller.checkForNewMessages();

		assertThat(testPublisher.getEvents().size(), is(1));
		assertThat(testPublisher.getLatestIncomingMessageEvent().getMessage().getFromNumber(), equalTo("+1231231234"));
		assertThat(testPublisher.getLatestIncomingMessageEvent().getMessage().getToNumber(), equalTo("+6543219876"));
		assertThat(testPublisher.getLatestIncomingMessageEvent().getMessage().getMessageText(), equalTo("Hi there! 🍺 and 🍻"));
	}

	protected static class MockResponse extends Response
	{
		private final int status;
		private final Object entity;

		public MockResponse(final int status, final Object entity)
		{
			super();
			this.status = status;
			this.entity = entity;
		}

		@Override
		public int getStatus()
		{
			return status;
		}

		@Override
		public StatusType getStatusInfo()
		{
			return Status.fromStatusCode(status);
		}

		@Override
		public Object getEntity()
		{
			return entity;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T readEntity(final Class<T> entityType)
		{
			return (T) entity;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T readEntity(final GenericType<T> entityType)
		{
			return (T) entity;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T readEntity(final Class<T> entityType, final Annotation[] annotations)
		{
			return (T) entity;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T readEntity(final GenericType<T> entityType, final Annotation[] annotations)
		{
			return (T) entity;
		}

		@Override
		public boolean hasEntity()
		{
			return entity != null;
		}

		@Override
		public boolean bufferEntity()
		{
			return true;
		}

		@Override
		public void close()
		{
			// nothing
		}

		@Override
		public MediaType getMediaType()
		{
			return null;
		}

		@Override
		public Locale getLanguage()
		{
			return null;
		}

		@Override
		public int getLength()
		{
			return 0;
		}

		@Override
		public Set<String> getAllowedMethods()
		{
			return null;
		}

		@Override
		public Map<String, NewCookie> getCookies()
		{
			return null;
		}

		@Override
		public EntityTag getEntityTag()
		{
			return null;
		}

		@Override
		public Date getDate()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Date getLastModified()
		{
			return null;
		}

		@Override
		public URI getLocation()
		{
			return null;
		}

		@Override
		public Set<Link> getLinks()
		{
			return null;
		}

		@Override
		public boolean hasLink(final String relation)
		{
			return false;
		}

		@Override
		public Link getLink(final String relation)
		{
			return null;
		}

		@Override
		public Builder getLinkBuilder(final String relation)
		{
			return null;
		}

		@Override
		public MultivaluedMap<String, Object> getMetadata()
		{
			return null;
		}

		@Override
		public MultivaluedMap<String, String> getStringHeaders()
		{
			return null;
		}

		@Override
		public String getHeaderString(final String name)
		{
			return null;
		}
	}
}

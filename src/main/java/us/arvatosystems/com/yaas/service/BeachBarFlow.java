package us.arvatosystems.com.yaas.service;

import us.arvatosystems.com.yaas.service.BeachBarFlowImpl.Conversation;
import us.arvatosystems.com.yaas.service.message.IncomingMessageEvent;

/**
 * Handles customer conversation via SMS messages.
 */
public interface BeachBarFlow
{
	/**
	 * Starts a new conversation.
	 *
	 * @param event the incoming message from the customer
	 * @return a new conversation context
	 */
	Conversation start(IncomingMessageEvent event);

	/**
	 * Continues with an existing conversation.
	 *
	 * @param ctx the current conversation context
	 * @param event the incoming message from the customer
	 */
	void proceed(Conversation ctx, IncomingMessageEvent event);

	/**
	 * A hook to be able to react to certain events such as when a conversation ends.
	 *
	 * @param callback The object to invoke on certain events.
	 */
	void setCallback(Callback callback);

	interface Callback
	{
		/**
		 * Gets invoked when the given conversation ended.
		 *
		 * @param conversation the conversation
		 */
		void onComplete(Conversation conversation);
	}
}
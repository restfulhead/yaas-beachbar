# Beach Bar API
The beach bar API allows you to order your favorite beverages via SMS.

This example shows how to use YaaS services to read SMS messages and send an automated reply using the arvato SMS service. Additionally new orders are posted to the hybris Order service.

More specifically, the arvato SMS service publishes new messages to the Hybris PubSub service. The Beach Bar API will check for new messages, process them, and send an automated reply by using the arvato SMS service REST API.

## Setup and configuration

### Your Builder project
To run this service you will need to setup a new YaaS project using the builder. Your project needs to subscribe to the following packages:
* arvato SMS service
* hybris Core service (for PubSub)
* hybris Commerce service (for order service)

You also need to create a new application in order to receive `clientId` and `clientSecret`.

Open `/src/main/resources/default.properties` and insert the `TENAT` (id of your project), `CLIENT_ID` and `CLIENT_SECRET`.

### SMS service
To be able to send and receive SMS messages, you need to first purchase a phone number. Please refer to the arvato SMS service for further details. 

When purchasing the number make sure that the `queue` flag is set to true. (Or use the Update API to do it at a later time). This will publish incoming SMS messages to the Pubsub service.

### Build and Run
Build using the following command: `mvn install`.

Run the web server with `mvn jetty:run`.

## Some technical notes
The example uses a simple finite state machine to track the state for each consumer (based on their phone number). Depending on the state, the service either sends a welcome message, a message to confirm the order or the order confirmation.

The API has to be able to identify the customer's request and to select the right product to order. This is implemented via very basic String matching and a few rules using Easy Rules. I plan to integrate a text recognition service for a better matching in future (contributions welcome!). 

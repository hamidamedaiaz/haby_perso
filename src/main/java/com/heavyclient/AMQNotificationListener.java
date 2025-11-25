package com.heavyclient;

import com.heavyclient.utils.RouteUtils;


import com.soap.generated.ItineraryData;
import com.soap.generated.ItineraryResult;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.jxmapviewer.viewer.GeoPosition;



import javax.jms.*;
import java.util.List;

public class AMQNotificationListener {

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String TOPIC_NAME = "notifications.global";
    private com.soap.generated.IRoutingService soapClient;
    private HeavyClientUI clientUI;

    public AMQNotificationListener(
            com.soap.generated.IRoutingService soapClient, HeavyClientUI clientUI
    ) {
        this.soapClient = soapClient;
        this.clientUI = clientUI;
    }

    public void StartListening() {
        try {
            // Create a ConnectionFactory
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);

            // Create a Connection
            Connection connection = connectionFactory.createConnection();
            connection.start();

            // Create a Session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create the Topic from which messages will be received
            Topic topic = session.createTopic(TOPIC_NAME);

            // Create a MessageConsumer for receiving messages
            MessageConsumer consumer = session.createConsumer(topic);

            // Set a MessageListener to handle incoming messages
            consumer.setMessageListener(new MessageListener() {
                public void onMessage(Message message) {
                    if (message instanceof TextMessage) {
                        try {
                            String text = ((TextMessage) message).getText();
                            System.out.println("Received notification: " + text);

                            if (text.contains("VÉLOS LIMITÉS")) {
                                System.out.println("⚠️ Recalcul automatique de l’itinéraire...");

                                clientUI.addNotification("⚠ Recalcul en cours…");

                                ItineraryResult newResult = soapClient.getItinerary(
                                        "45.758", "4.835", "Lyon",
                                        "48.8566", "2.3522", "Paris"
                                );

                                ItineraryData newData = newResult.getData().getValue();
                                List<GeoPosition> newRoute = RouteUtils.extractRoute(newData);

                                clientUI.showRoute(newRoute);
                                // on peut réappler le Routing Server SOAP ici pour recalculer l'itinéraire
                            }

                        } catch (JMSException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            System.out.println("Listening for notifications on topic: " + TOPIC_NAME);

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}

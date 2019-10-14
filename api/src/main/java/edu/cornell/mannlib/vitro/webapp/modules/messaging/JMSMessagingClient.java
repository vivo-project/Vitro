package edu.cornell.mannlib.vitro.webapp.modules.messaging;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class JMSMessagingClient {

    private static final Log log = LogFactory.getLog(JMSMessagingClient.class);

    private String factoryInitial;

    private String providerURL;

    private String connectionFactory;

    private String brokerDestination;

    private String brokerUsername;

    private String brokerPassword;

    private Connection connection;

    private Destination destination;

    private Session session;

    private MessageProducer producer;

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasFactoryInitial", minOccurs = 1, maxOccurs = 1)
    public void setFactoryInitial(String factoryInitial) {
        this.factoryInitial = factoryInitial;
    }

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasProviderURL", minOccurs = 1, maxOccurs = 1)
    public void setProviderURL(String providerURL) {
        this.providerURL = providerURL;
    }

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasConnectionFactory", minOccurs = 1, maxOccurs = 1)
    public void setConnectionFactory(String connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasBrokerDestination", minOccurs = 1, maxOccurs = 1)
    public void setBrokerDestination(String destination) {
        this.brokerDestination = destination;
    }

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasBrokerUsername", minOccurs = 1, maxOccurs = 1)
    public void setBrokerUsername(String username) {
        this.brokerUsername = username;
    }

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasBrokerPassword", minOccurs = 1, maxOccurs = 1)
    public void setBrokerPassword(String password) {
        this.brokerPassword = password;
    }

    public void startup(Application application, ComponentStartupStatus ss) {
        Properties properties = new Properties();
        properties.put("java.naming.factory.initial", factoryInitial);
        properties.put("java.naming.provider.url", providerURL);

        String[] destinationParts = brokerDestination.split("/");
        String destinationBinding = String.format("%s.%s", destinationParts[0], brokerDestination);
        String destinationName = destinationParts[1];
        properties.put(destinationBinding, destinationName);

        try {
            InitialContext ic = new InitialContext(properties);
            ConnectionFactory cf = (ConnectionFactory) ic.lookup(connectionFactory);

            destination = (Destination) ic.lookup(brokerDestination);
            connection = cf.createConnection(brokerUsername, brokerPassword);
            session = connection.createSession(Session.AUTO_ACKNOWLEDGE);
            producer = session.createProducer(destination);

            log.info(String.format("Message producer connected to %s at %s", brokerDestination, providerURL));
        } catch (NamingException | JMSException e) {
            log.error(e, e);
        }

    }

    public void send(String payload) throws JMSException {
        log.debug(String.format("Sending message to %s:\n%s", brokerDestination, payload));
        TextMessage message = session.createTextMessage(payload);
        producer.send(message);
    }

}

package edu.cornell.mannlib.vitro.webapp.modules.messaging;

import static javax.naming.Context.INITIAL_CONTEXT_FACTORY;
import static javax.naming.Context.PROVIDER_URL;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private String brokerType;

    private String brokerDestination;

    private String brokerUsername;

    private String brokerPassword;

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

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasBrokerType", minOccurs = 1, maxOccurs = 1)
    public void setBrokerType(String type) {
        this.brokerType = type;
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
        Pattern pattern = Pattern.compile("^.*/(.*)$");
        Matcher matcher = pattern.matcher(brokerDestination);
        if (matcher.find()) {
            String destinationName = matcher.group(1);
            String destinationBinding = String.format("%s.%s", brokerType, brokerDestination);

            Properties properties = new Properties();
            properties.put(INITIAL_CONTEXT_FACTORY, factoryInitial);
            properties.put(PROVIDER_URL, providerURL);
            properties.put(destinationBinding, destinationName);

            try {
                InitialContext ic = new InitialContext(properties);
                ConnectionFactory cf = (ConnectionFactory) ic.lookup(connectionFactory);
    
                Destination destination = (Destination) ic.lookup(brokerDestination);
                Connection connection = cf.createConnection(brokerUsername, brokerPassword);
                session = connection.createSession(Session.AUTO_ACKNOWLEDGE);
                producer = session.createProducer(destination);
    
                ss.info(String.format("Connected to %s at %s", brokerDestination, providerURL));
            } catch (NamingException | JMSException e) {
                ss.warning(String.format("Failed to connect to %s at %s", brokerDestination, providerURL), e);
            }
        } else {
            ss.warning(String.format("%s is not a valid broker destination", brokerDestination));
        }
    }

    public void send(String payload) throws JMSException {
        log.debug(String.format("Sending message to %s:\n%s", brokerDestination, payload));
        TextMessage message = session.createTextMessage(payload);
        producer.send(message);
    }

}
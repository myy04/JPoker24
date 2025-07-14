import java.util.Random;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Topic; // Changed from Queue to Topic
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class MessageSender {
    
    private Context jndiContext;
    private String host = "localhost";
    private ConnectionFactory connectionFactory;
    private Topic topic; // Changed from Queue to Topic
    private Connection connection;
    private Session session;
    private MessageProducer producer;

    public MessageSender() throws NamingException, JMSException { 
        System.setProperty("org.omg.CORBA.ORBInitialHost", host);
        System.setProperty("org.omg.CORBA.ORBInitialPort", "3700");
        try {
            jndiContext = new InitialContext();
        } catch (NamingException e) {
            System.err.println("Could not create JNDI API context: " + e);
            throw e;
        }
        
        // Lookup JMS resources
        lookupConnectionFactory();
        lookupTopic(); // Changed from lookupQueue()
        
        // Create connection->session->producer
        createConnection();
        createSession();
        createProducer();
    }
    
    private void lookupConnectionFactory() throws NamingException {
        try {
            connectionFactory = (ConnectionFactory)jndiContext.lookup("jms/JPoker24ConnectionFactory");
        } catch (NamingException e) {
            System.err.println("JNDI API JMS connection factory lookup failed: " + e);
            throw e;
        }
    }
    
    private void lookupTopic() throws NamingException {
        try {
            topic = (Topic)jndiContext.lookup("jms/JPoker24GameTopic");
        } catch (NamingException e) {
            System.err.println("JNDI API JMS topic lookup failed: " + e);
            throw e;
        }
    }
    
    private void createConnection() throws JMSException {
        try {
            connection = connectionFactory.createConnection();
            connection.start();
        } catch (JMSException e) {
            System.err.println("Failed to create connection to JMS provider: " + e);
            throw e;
        }
    }
    
    private void createSession() throws JMSException {
        try {
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
            System.err.println("Failed to create session: " + e);
            throw e;
        }
    }
    
    private void createProducer() throws JMSException {
        try {
            producer = session.createProducer(topic); // Now using topic instead of queue
        } catch (JMSException e) {
            System.err.println("Failed to create message producer: " + e);
            throw e;
        }
    }
    
    public void sendMessage(String message) {
        try {
            TextMessage textMessage = session.createTextMessage(message);
            producer.send(textMessage);
            System.out.println("Sent message: " + message);
        }
        catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }
    
    public void close() {
        try {
            if (producer != null) producer.close();
            if (session != null) session.close();
            if (connection != null) connection.close();
        } catch (JMSException e) {
            System.err.println("Error closing JMS resources: " + e.getMessage());
        }
    }
}
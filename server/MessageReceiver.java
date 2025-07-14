import javax.jms.Queue;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.jms.Message;  // Add this import


public class MessageReceiver {
    private Context jndiContext;
    private String host = "localhost";
    private ConnectionFactory connectionFactory;
    private Queue queue;
    private Connection connection;
    private Session session;
    private MessageConsumer consumer;
    private boolean running = false;
    private Thread listenerThread;
    private Server server = null;

    public MessageReceiver(Server server) throws NamingException, JMSException {
        
        this.server = server;

        createJNDIContext();

        // Lookup JMS resources
        lookupConnectionFactory();
        lookupQueue();
        
        // Create connection->session->consumer
        createConnection();
        createSession();
        createConsumer();
    }

    private void createJNDIContext() throws NamingException {
        try {
            // Try simple InitialContext first
            jndiContext = new InitialContext();
        } catch (NamingException e) {
            System.err.println("Simple JNDI context failed, trying with properties: " + e.getMessage());
            
            // Fallback to properties-based context
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY, 
                             "com.sun.jndi.fscontext.RefFSContextFactory");
            props.setProperty(Context.PROVIDER_URL, "file:///tmp");
            
            try {
                jndiContext = new InitialContext(props);
            } catch (NamingException e2) {
                System.err.println("Could not create JNDI API context: " + e2);
                throw e2;
            }
        }
    }
    
    private void lookupConnectionFactory() throws NamingException {
        try {
            connectionFactory = (ConnectionFactory)jndiContext.lookup("jms/JPoker24ConnectionFactory");
        } catch (NamingException e) {
            System.err.println("JNDI API JMS connection factory lookup failed: " + e);
            throw e;
        }
    }
    
    private void lookupQueue() throws NamingException {
        try {
            queue = (Queue)jndiContext.lookup("jms/JPoker24GameQueue");
        } catch (NamingException e) {
            System.err.println("JNDI API JMS queue lookup failed: " + e);
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
    
    private void createConsumer() throws JMSException {
        try {
            consumer = session.createConsumer(queue);
        } catch (JMSException e) {
            System.err.println("Failed to create message consumer: " + e);
            throw e;
        }
    }
    
    public void startReceiving() {
        if (running) return;
        
        running = true;
        
        listenerThread = new Thread(() -> {
            try {
                while (running) {
                    Message message = consumer.receive(1000); // 1 second timeout
                    if (message != null && message instanceof TextMessage) {
                        processMessage((TextMessage) message);
                    }
                }
            } catch (JMSException e) {
                if (running) {
                    System.err.println("Error receiving messages: " + e.getMessage());
                }
            }
        });
        
        listenerThread.start();
        System.out.println("Started receiving messages");
    }
    
    public void stopReceiving() {
        running = false;
        if (listenerThread != null) {
            try {
                listenerThread.join(2000); // Wait up to 2 seconds for thread to finish
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Stopped receiving messages");
    }
    
    public void close() {
        stopReceiving();
        try {
            if (consumer != null) consumer.close();
            if (session != null) session.close();
            if (connection != null) connection.close();
            System.out.println("Closed all JMS resources");
        } catch (JMSException e) {
            System.err.println("Error closing JMS resources: " + e.getMessage());
        }
    }
    
    private void processMessage(TextMessage textMessage) throws JMSException {
        String messageText = textMessage.getText();
        System.out.println("Received message: " + messageText);
        
        // Parse the message - format is "username#answer#answerTime"
        try {
            String[] parts = messageText.split("#");

            String command = parts[0];

            if (command.equals("ANSWER")) {
                String username = parts[1];
                String answer = parts[2];
                double answerTime = Double.parseDouble(parts[3]);            
            
                processAnswer(username, answer, answerTime);
            }
            else if (command.equals("JOIN")) {
                String username = parts[1];
                processJoin(username);
            }
            else {
                System.err.println("Invalid message format: " + messageText);
                return;
            }            
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    private void processAnswer(String username, String answer, double answerTime) {
        server.processAnswer(username, answer, answerTime);
    }

    private void processJoin(String username) {
        server.processJoin(username);
    }
}
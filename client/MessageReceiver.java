import java.util.ArrayList;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class MessageReceiver {
    private Context jndiContext;
    private String host = "localhost";
    private ConnectionFactory connectionFactory;
    private Topic topic;
    private Connection connection;
    private Session session;
    private MessageConsumer consumer;
    private boolean running = false;
    private Thread listenerThread;
    private Client client;
    
    public MessageReceiver(Client client) throws NamingException, JMSException {
        this.client = client;
        
        // Set up JNDI context
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
        lookupTopic();
        
        // Create connection->session->consumer
        createConnection();
        createSession();
        createConsumer();
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
            connection.setClientID(client.getUsername()); // Set a unique client ID for durable subscription
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
            // Create a durable subscriber to ensure messages aren't missed
            consumer = session.createDurableSubscriber(topic, "subscription-" + client.getUsername());
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
        System.out.println("Started receiving messages from topic");
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
        
        // Parse the message - expected format: "command#data"
        try {
            String[] parts = messageText.split("#");
            if (parts.length >= 2) {
                String command = parts[0];
                
                if (command.equals("START")) {
                    // Process start command
                    String[] usernames = parts[1].split(",");
                    processStart(usernames);
                } 
                else if (command.equals("ANSWER")) {
                    // Process answer command
                    String[] answer = parts[1].split(",");
                    processAnswer(answer);
                } 
                else if (command.equals("WIN")) {
                    String username = parts[1];
                    processWin(username);
                }
                else {
                    System.err.println("Unknown command: " + command);
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }
    
    private void processStart(String[] usernames) {
        ArrayList<String> session = new ArrayList<> ();

        for (String username : usernames) { 
            session.add(username);
        }

        if (session.contains(client.getUsername())) {
            client.startGame(session);
            System.out.println("Game started with players: " + session);
        }
    }

    private void processAnswer(String[] answer) {
        String username = answer[0];
        String result = answer[1];
        double answerTime = Double.parseDouble(answer[2]);
        
        System.out.println("Answer received from " + username + ": " + result + " in " + answerTime + " seconds");
    }

    private void processWin(String username) {
        if (client.getSession().contains(username)) {
            client.stopGame();
        }
    }
}
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.jms.JMSException;
import javax.naming.NamingException;

public class Server extends UnicastRemoteObject implements ServerInterface {

    private static Database database;
    private static MessageReceiver messageReceiver;
    private static MessageSender messageSender;

    // Constructor with RemoteException
    public Server() throws Exception {
        super();
        database = new Database();
        database.clearOnlineUser();

        try {
            messageReceiver = new MessageReceiver(this); 
            messageReceiver.startReceiving();

            messageSender = new MessageSender();
        }
        catch (Exception e) {
            System.err.println("Error initializing: " + e.getMessage());
        }
    }

    // Method to register a new user
    @Override
    public boolean registerUser(String username, String password) throws RemoteException {
        System.out.println("In process: " + username + " registration");

        boolean ret = false;
        try {
            if (database.checkRegisteredUser(username) == false) {
                database.addRegisteredUser(username, password);
                database.commit();
                ret = true;
            }
        }

        catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        if (ret) System.out.println("Success: " + username + " registration");
        else System.out.println("Failure: " + username + " registration");
        
        return ret;
    }
    
    // Method to log in an user
    @Override
    public boolean loginUser(String username, String password) throws RemoteException {
        System.out.println("In process: " + username + " log in");

        boolean ret = false;

        try {
            if (database.checkRegisteredUser(username) == true && database.checkOnlineUser(username) == false
            && database.getUserPassword(username).equals(password)) {
                database.addOnlineUser(username);
                database.commit();
                ret = true;
            }
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        if (ret) System.out.println("Success: " + username + " log in");
        else System.out.println("Failure: " + username + " log in");

        return ret;
    }


    // Method to log out an user
    @Override
    public void logoutUser(String name, String password) throws RemoteException {
        System.out.println("In process: " + name + " log out");
        
        boolean complete = false;
        try {
            if (database.checkOnlineUser(name)) {
                database.removeOnlineUser(name);
                database.commit();
                complete = true;
            } 
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        if (complete) System.out.println("Success: " + name + " log out");
        else System.out.println("Failure: " + name + " log out");
    }
    

    public static void main(String[] args) {
        try {
            // Set the security manager with the policy file
            System.setProperty("java.security.policy", "server.policy");
            System.setProperty("com.mysql.cj.disableAbandonedConnectionCleanup", "true");
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }
            
            // Create and register the server
            Server server = new Server();
            Registry registry = LocateRegistry.createRegistry(1098);
            registry.rebind("ServerService", server);
            
            System.out.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }


    private ArrayList<ArrayList<String>> sessions = new ArrayList<>();

    public void processAnswer(String username, String answer, double answerTime) {
        boolean valid = false;
        try {
            valid = ExpressionEvaluator.evaluateExpression(answer) == 24;
        }
        catch (Exception e) {
            System.err.println("Error evaluating expression: " + e.getMessage());
        }

        // messageSender.sendMessage("answer#" + username + "," + (valid ? "correct" : "incorrect") + "," + answerTime);   
    
        if (valid) {
            messageSender.sendMessage("WIN#" + username);
        }
    }

    private static ArrayList<String> waitingUsers = new ArrayList<>();
    public void processJoin(String username) {
        if (waitingUsers.contains(username)) {
            
            String anotherPlayer = null;
            for (String player : waitingUsers) {
                if (player.equals(username) == false) {
                    anotherPlayer = player;
                    break;
                }
            }

            if (anotherPlayer != null) {
                messageSender.sendMessage("START#" + anotherPlayer + "," + username);
         
                waitingUsers.remove(username);
                waitingUsers.remove(anotherPlayer);
            }

            return;
        }

        waitingUsers.add(username);

        if (waitingUsers.size() >= 4) {
            StringBuilder sb = new StringBuilder("START#");
            for (String user : waitingUsers) {
                sb.append(user).append(",");
            }
            // Remove the last comma
            sb.setLength(sb.length() - 1);
            
            messageSender.sendMessage(sb.toString());
            waitingUsers.clear();
        }

    }


}
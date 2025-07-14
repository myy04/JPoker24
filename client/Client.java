import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Queue;
import java.sql.Timestamp;

public class Client {

    private static ServerInterface server;
    
    private String username;
    private String password;

    private Timestamp startTime;

    private MessageSender messageSender;
    private MessageReceiver messageReceiver;

    private ArrayList<String> session; 

    public ClientUI ui;

    public Client() {
        try {
            // Get the registry
            Registry registry = LocateRegistry.getRegistry("localhost", 1098);
            
            // Look up the remote object
            server = (ServerInterface) registry.lookup("ServerService");
            
            ui = new ClientUI(this);

            System.out.println("Connected to the server successfully");

        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void logoutUser() {
        try {
            server.logoutUser(this.getUsername(), this.getPassword());

            setUsername(null);
            setPassword(null);

            if (messageSender != null) {
                messageSender.close();
                messageSender = null;
            }
            
            if (messageReceiver != null) {
                messageReceiver.stopReceiving();
                messageReceiver = null;
            }

            ui.frame.dispose();
            ui.startLoginWindow();


        } catch (RemoteException e) {
            System.err.println("Error logging out user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean registerUser(String userusername, String password) {
        try {
            return server.registerUser(userusername, password);
        } catch (RemoteException e) {
            System.err.println("Error registering user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean loginUser(String userusername, String password) {
        try {
            if (server.loginUser(userusername, password)) {
                setUsername(userusername);
                setPassword(password);

                messageSender = new MessageSender();

                
                messageReceiver = new MessageReceiver(this);
                messageReceiver.startReceiving();


                ui.frame.dispose();
                ui.startMainWindow();

                return true;
            }
            else return false;
        } catch (Exception e) {
            System.err.println("Error logging in user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public String[][] getCards() {
        String[][] cards = new String[4][2];
        cards[0][0] = "A";
        cards[0][1] = "hearts";
        cards[1][0] = "K";
        cards[1][1] = "diamonds";
        cards[2][0] = "Q";
        cards[2][1] = "clubs";
        cards[3][0] = "J";
        cards[3][1] = "spades";
        return cards;
    }

    public Player[] getPlayers() {
        Player[] players = new Player[4];
        players[0] = new Player("Player1", 5, 10, 2.5);
        players[1] = new Player("Player2", 3, 8, 3.0);
        players[2] = new Player("Player3", 7, 12, 2.0);
        players[3] = new Player("Player4", 4, 9, 2.8);
        return players;
    }

    public void setTimestamp() {
        startTime = new Timestamp(System.currentTimeMillis());
        System.out.println("Timer started at: " + startTime);
    }

    public double getAnswerTime() {
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        double elapsedTime = currentTime.getTime() - startTime.getTime();
        System.out.println("Elapsed time: " + elapsedTime + " milliseconds");
        return elapsedTime;
    }

    public void sendAnswer(String answer) {
        try {
            double answerTime = getAnswerTime();
            System.out.println("Answer sent: " + answer);
            System.out.println("Answer time: " + answerTime);

            messageSender.sendMessage("ANSWER" + "#" + this.getUsername() + "#" + answer + "#" + answerTime);
        }
        catch (Exception e) {
            System.err.println("Error sending answer: " + e.getMessage());
        }
    }

    private boolean gameStarted = false;

    public ArrayList<String> getSession() {
        return session;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void startGame(ArrayList<String> usernames) {
        this.gameStarted = true;
        System.out.println("Game started");
        this.session = usernames;

        ui.frame.dispose();
        ui.startGameWindow();
    }

    public void stopGame() {
        this.gameStarted = false;
        System.out.println("Game stopped");
        this.session = null;

        ui.frame.dispose();
        ui.startMainWindow();
    }

    public boolean joinGame() {
        ui.frame.dispose();
        ui.startWaitingGameWindow();        

        boolean ret = false;
        try {
            System.out.println("Attempting to join game...");
            
            messageSender.sendMessage("JOIN" + "#" + this.getUsername());
        
            int timer = 0;
            while (gameStarted == false) {
                // Wait for the game to start
                Thread.sleep(1000);
                timer += 1;
                if (timer == 10) {
                    messageSender.sendMessage("JOIN" + "#" + this.getUsername());
                }

                if (timer >= 20) break;
            }

            ret = gameStarted;
        }
        catch (Exception e) {
            System.err.println("Error joining game: " + e.getMessage());
        }

        return ret;
    }


    public static void main(String[] args) {
        Client client = new Client();

        client.ui.startLoginWindow();
    }
}
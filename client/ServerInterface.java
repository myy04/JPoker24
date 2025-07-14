import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
    boolean loginUser(String username, String password) throws RemoteException; // Method to log in a remote user
    boolean registerUser(String username, String password) throws RemoteException; // Method to register a remote user
    void logoutUser(String username, String password) throws RemoteException; // Method to log out a remote user
}

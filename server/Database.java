import java.sql.*;


public class Database {

    private static final String DB_HOST = "localhost";
	private static final String DB_USER = "c3358";
	private static final String DB_PASS = "c3358PASS";
	private static final String DB_NAME = "assignment";

    private static Connection conn;

    // TABLES: UserInfo, OnlineUser

    public Database() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		Class.forName("com.mysql.cj.jdbc.Driver");
		String url = "jdbc:mysql://" + DB_HOST + ":3306/" + DB_NAME;
        conn = DriverManager.getConnection(url, DB_USER, DB_PASS);
        conn.setAutoCommit(false);
        System.out.println("Connected to database");
    }

    public void commit() {
        try {
            conn.commit();
            System.out.println("Committed");
        } 
        catch (SQLException e) {
            System.err.println(e.getMessage());

            try {
                conn.rollback();
            } 
            catch (SQLException ee) {
                System.err.println(ee.getMessage());
            }

            System.err.println("Error during committing");
        }
    }

    public void closeConenction() throws SQLException {
        conn.close();
        System.out.println("Connection is closed");
    }

    public void addRegisteredUser(String username, String password) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO UserInfo (username, password) VALUES (?, ?)");
        stmt.setString(1, username);
        stmt.setString(2, password);
        stmt.executeUpdate();  
        System.out.println(username + " is added to UserInfo");
    }

    public boolean checkRegisteredUser(String username) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM UserInfo WHERE username = ?");
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            System.out.println(username + " is found in UserInfo");
            return true;
        }
        else {
            System.out.println(username + " is not found in UserInfo");
            return false;        
        }
    }

    public String getUserPassword(String username) throws SQLException {    
        PreparedStatement stmt = conn.prepareStatement("SELECT password FROM UserInfo WHERE username = ?");
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            System.out.println("Password for " + username + " is found in UserInfo");
            return rs.getString("password");
        }
        else {
            System.out.println("Password for " + username + "is not found in UserInfo");
            return null;
        }
    }

    public void addOnlineUser(String username) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO OnlineUser (username) VALUES (?)");
        stmt.setString(1, username);
        stmt.executeUpdate();  

        System.out.println(username + " is added to OnlineUser");
    }

    public boolean checkOnlineUser(String username) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM OnlineUser WHERE username = ?");
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            System.out.println(username + " is found in OnlineUser");
            return true;
        }   
        else {
            System.out.println(username + " is not found in OnlineUser");
            return false;
        }
    }

    public void removeOnlineUser(String username) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM OnlineUser WHERE username = ?");
        stmt.setString(1, username);
        stmt.executeUpdate();  
        System.out.println(username + " is removed from OnlineUser");
    }

    public void clearOnlineUser() throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM OnlineUser");
        stmt.executeUpdate();  
        System.out.println("All users are removed from OnlineUser");
    }

}

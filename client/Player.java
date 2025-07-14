public class Player {

    private String username;
    private int totalGames;
    private int wins;
    private double averageTime;

    public Player(String username, int totalGames, int wins, double averageTime) {
        this.username = username;
        this.totalGames = totalGames;
        this.wins = wins;
        this.averageTime = averageTime;
    }

    public String getUsername() {
        return username;
    }

    public int getTotalGames() {
        return totalGames;
    }

    public int getWins() {
        return wins;
    }

    public double getAverageTime() {
        return averageTime;
    }
}   

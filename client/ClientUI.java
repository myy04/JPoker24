import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ClientUI {
    public JFrame frame;
    private Client client;

    public ClientUI(Client client) {
        this.client = client;
    }
    
    public void startLoginWindow() {
        frame = new JFrame("JPoker 24");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new BorderLayout(10, 10));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JLabel lblUsername = new JLabel("Username:");
        JTextField txtUsername = new JTextField(15);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(lblUsername, gbc);
        
        gbc.gridx = 1;
        formPanel.add(txtUsername, gbc);

        JLabel lblPassword = new JLabel("Password:");
        JPasswordField txtPassword = new JPasswordField(15);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(lblPassword, gbc);
        
        gbc.gridx = 1;
        formPanel.add(txtPassword, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLogin = new JButton("Login");
        JButton btnRegister = new JButton("Register");

        btnLogin.addActionListener(e -> {    
            String username = txtUsername.getText();
            String password = new String(txtPassword.getPassword());

            if (client.loginUser(username, password) == false) {
                JOptionPane.showMessageDialog(frame, "Login failed", "Error", JOptionPane.ERROR_MESSAGE);    
            }
        });

        btnRegister.addActionListener(e -> {
            
            String username = txtUsername.getText();
            String password = new String(txtPassword.getPassword());

            if (password.length() < 3) {
                JOptionPane.showMessageDialog(frame, "Password must be at least 3 characters long", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (client.registerUser(username, password)) {
                JOptionPane.showMessageDialog(frame, "Registration successful", "Success", JOptionPane.INFORMATION_MESSAGE);

                client.loginUser(username, password);
            }
            else {
                JOptionPane.showMessageDialog(frame, "Registration failed", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(btnLogin);
        buttonPanel.add(btnRegister);
        
        frame.add(formPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void startMainWindow() {
        frame = new JFrame("JPoker 24: " + client.getUsername());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.logoutUser();
                System.exit(0);
            }
        });

        frame.setSize(1280, 720);

        JTabbedPane tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab("User Profile", this.getUserProfilePanel());
        tabbedPane.addTab("Play Game", this.getPlayGamePanel());
        tabbedPane.addTab("Leader Board", this.getLeaderBoardPanel());
        frame.add(tabbedPane);


        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPanel.add(this.getLogoutButton());
        frame.add(logoutPanel, BorderLayout.NORTH);

        frame.setLocationRelativeTo(null);   
        frame.setVisible(true);
    }

    private JPanel getUserProfilePanel() {
        JPanel userProfilePanel = new JPanel();
        userProfilePanel.setLayout(new BoxLayout(userProfilePanel, BoxLayout.Y_AXIS));
        userProfilePanel.setBackground(Color.WHITE);
        
        JLabel nameLabel = new JLabel(client.getUsername());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        userProfilePanel.add(Box.createVerticalStrut(20));
        userProfilePanel.add(nameLabel);
        
        userProfilePanel.add(Box.createVerticalStrut(10));
        userProfilePanel.add(new JLabel("Number of wins: 10"));
        userProfilePanel.add(Box.createVerticalStrut(10));
        userProfilePanel.add(new JLabel("Average game time: 12.5s"));
        userProfilePanel.add(Box.createVerticalStrut(10));
        userProfilePanel.add(new JLabel("Rank: #10"));
        
        return userProfilePanel;
    }

    private JPanel getPlayGamePanel() {
        JPanel playGamePanel = new JPanel();
        playGamePanel.setLayout(new BorderLayout());
        
        JButton playButton = new JButton("Play Game");

        playButton.addActionListener(e -> {
            client.joinGame();
        });

        playGamePanel.add(playButton, BorderLayout.CENTER);
        return playGamePanel;
    }

    private JPanel getLeaderBoardPanel() {
        JPanel leaderBoardPanel = new JPanel();
        leaderBoardPanel.setLayout(new BorderLayout());
        leaderBoardPanel.setBackground(Color.WHITE);

        String[] columnNames = {"Rank", "Player", "Games won", "Games played", "Avg. winning time"};
        Object[][] data = {
            {1, "Player 1", 20, 30, "10.4s"},
            {2, "Player 2", 18, 25, "13.2s"},
            {3, "Player 8", 15, 31, "15.1s"},
            {4, "Player 7", 10, 20, "12.8s"},
            {5, "Player 6", 10, 25, "12.5s"},
            {6, "Player 3", 7, 15, "17.4s"},
            {7, "Player 5", 4, 10, "15.4s"},
            {8, "Player 10", 2, 12, "16.2s"},
            {9, "Player 9", 1, 4, "14.1s"},
            {10, "Player 4", 1, 10, "11.4s"}
        };

        JTable table = new JTable(data, columnNames);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);

        leaderBoardPanel.add(scrollPane, BorderLayout.CENTER);

        return leaderBoardPanel;
    }

    private JButton getLogoutButton() {
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            client.logoutUser();            
        });
        
        return logoutButton;
    }

    public void startWaitingGameWindow() {
        frame = new JFrame("JPoker 24: " + client.getUsername());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        
        JLabel waitingLabel = new JLabel("Waiting for other players to join...");      
        frame.add(waitingLabel);
        
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }


    public void startGameWindow() {
        frame = new JFrame("JPoker 24: " + client.getUsername());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280, 720);
    
        frame.add(getCardsPanel(client.getCards()), BorderLayout.CENTER);
        frame.add(getInputPanel(), BorderLayout.SOUTH);
        frame.add(getPlayersPanel(client.getPlayers()), BorderLayout.EAST);


        frame.setLocationRelativeTo(null);   
        frame.setVisible(true);

        client.setTimestamp();
    }

    private JPanel getCardsPanel(String[][] cards) {   
        class Local {
            private ImageIcon getCardImg(String rank, String suit) {
                String imgPath = "/playing-cards-master/" + suit + "_" + rank + ".png";
                return new ImageIcon(getClass().getResource(imgPath));
            }        
        }

        ImageIcon[] cardImages = new ImageIcon[4];
        Local local = new Local();

        for (int i = 0; i < 4; i++) {
            cardImages[i] = local.getCardImg(cards[i][0], cards[i][1]);
        }

        JPanel cardsPanel = new JPanel();
        cardsPanel.setLayout(new GridLayout(1, 4));
        for (int i = 0; i < 4; i++) {
            cardsPanel.add(new javax.swing.JLabel(cardImages[i]));
        }

        return cardsPanel;   
    }

    private JPanel getPlayersPanel(Player[] player) {
        class Local {
            private JPanel getPlayerPanel(Player player) {
                JPanel playerPanel = new JPanel();
                playerPanel.setLayout(new GridLayout(2, 2));
               
                playerPanel.setBorder(BorderFactory.createTitledBorder(player.getUsername()));
        
                playerPanel.add(new javax.swing.JLabel("Wins: " + player.getWins() + "/" + player.getTotalGames()));
                playerPanel.add(new javax.swing.JLabel(player.getAverageTime() + " seconds"));
        

                return playerPanel;
            }
        }

        JPanel playersPanel = new JPanel();
        Local local = new Local();

        for (int i = 0; i < player.length; i++) {
            playersPanel.add(local.getPlayerPanel(player[i]));
        }

        playersPanel.setLayout(new java.awt.GridLayout(player.length, 0));
        return playersPanel;

    }

    private JPanel getInputPanel() {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new java.awt.GridLayout(1, 2));

        JTextField inputField = new JTextField();
        inputField.addActionListener(e -> {
            String input = inputField.getText();
            client.sendAnswer(input);
            inputField.setText(""); // Clear the field after sending
        });

        JButton submitButton = new JButton("Submit");

        submitButton.addActionListener(e -> {
            String input = inputField.getText();
            client.sendAnswer(input);
        });

        inputPanel.add(inputField);
        inputPanel.add(submitButton);

        return inputPanel;
    }

}
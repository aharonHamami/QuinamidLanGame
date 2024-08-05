package default_package;

import java.awt.Toolkit;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Server {

    private final String PLAYER_NET_OP = "A real player";
    private final String PLAYER_AI_OP = "AI player";
    
    private final String COLOR_RED_OP = "red";
    private final String COLOR_BLUE_OP = "blue";
    
    // view:
    public static JFrame window;
    public static JTextArea infoArea;
    public static JTextField inputArea;

    // server:
    private ServerSocket serverSocket;
    private ArrayList<Game> gameList; // a list of all the games that currently playing
    private ArrayList<PlayerNet> playerNetList; // a list of all the connected client players
    private AppSocket waitingGuest; // socket for a client that waiting for a new participant to play with him
        
    public static void main(String[] args){
        buildView();
        Server server = new Server();
        server.start();
    }
    
    public Server() {
        gameList = new ArrayList<Game>();
        playerNetList = new ArrayList<PlayerNet>();
        waitingGuest = null;
        
        serverSocket = null;
        int port = 3000;
        String question = "choose which port to listen to (default=3000):";
        while(serverSocket == null) {
            String portStr = JOptionPane.showInputDialog(window, question, "3000");
            if(portStr == null) {
                System.exit(0);
            }else {
                try {
                    port = Integer.parseInt(portStr);
                }catch (Exception e) {
                    question = "this port is not valid, try again";
                    continue;
                }
            }
            
            try {
                serverSocket = new ServerSocket(port);
            } catch (IOException ex) {
                question = "Couldn't connect to port "+ port +", try another one";
                continue;
            }
            
            String localHost = null;
            try {
                localHost = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            print("listen on " + localHost + ":" + port);
        }
        
    }
    
    /**
     * build and open a new window for displaying text
     */
    public static void buildView(){
        window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLayout(new BorderLayout());
        window.setSize(600, 500);
        int locationX = (int)(0.5*(Toolkit.getDefaultToolkit().getScreenSize().width - window.getWidth())); // in the middle
        int locationY = (int)(0.5*(Toolkit.getDefaultToolkit().getScreenSize().height - window.getHeight())); // in the middle
        window.setLocation(locationX, locationY);
        window.setTitle("server");
        
        infoArea = new JTextArea();
        infoArea.setFont(new Font(null, 0, 20));
        JPanel editPanel = new JPanel();
        inputArea = new JTextField();
        JButton sendButton = new JButton("send");
               
        infoArea.setSize(600, 500);
        editPanel.setSize(600, 200);
        infoArea.setBackground(Color.black);
        infoArea.setForeground(Color.green);
//        sendButton.setSize(100, sendButton.getHeight());
        editPanel.setLayout(new GridLayout(1, 2));
        
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                infoArea.append("you: "+inputArea.getText());
                inputArea.setText("");
            }
        });
        
        window.add(infoArea, BorderLayout.CENTER);
        
        JScrollPane scroll = new JScrollPane(infoArea, 
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        window.add(scroll, BorderLayout.CENTER);
        
        editPanel.add(inputArea);
        editPanel.add(sendButton);
//        window.add(editPanel, BorderLayout.SOUTH);
        window.setVisible(true);
    }
    
    /**
     * start the server and listen to new clients
     */
    public void start() {
        if(serverSocket == null) {
            return;
        }
        // start running:
        print("wait for connectoions...");
        print("\n---------\n");
        
        while(true){
            try {
                
                Socket msgClientSocket = serverSocket.accept(); // for messages running on the game (socket.readMessage())
                Socket cmdClientSocket = serverSocket.accept(); // for commands for actions out of the game (socket.readCommand())
                print("two sockets on: "+msgClientSocket+" || "+cmdClientSocket);
                
                // thread for handling clients in the backround while letting the corrent thread to accept new clients
                Thread clientThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AppSocket appSocket = new AppSocket(msgClientSocket, cmdClientSocket);
                        handleClient(appSocket);
                    }
                });
                clientThread.start();
                
            } catch (IOException ex) {
                print("error while trying to connect to the clients");
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * create a new PlayerNet and update the PlayerNetList
     * @param name the name of the player
     * @param socket the socket to the client that the player will be connected to
     * @return the player created
     */
    private PlayerNet createPlayerNet(String name, AppSocket socket) {
        PlayerNet player = new PlayerNet(name, socket);
        
        synchronized(playerNetList) {
            playerNetList.add(player);
        }
        
        return player;
    }
    
    /**
     * handle new clients that connected to the server
     * @param appSocket the socket to the client
     */
    private void handleClient(AppSocket appSocket){
        print("new guest connected");
        
        // thread to read commands from the client in the background - letting me to continue client handling
        Thread commandThread = new Thread(() -> {
            boolean keepReading = true;
            while(keepReading) {
                try {
                    Message command = appSocket.readCommand();
                    print("command: "+command.getSubject());

                    switch(command.getSubject()) {
                        case Message.DISCONNECT:
                            print("Making a normal disconnection");
                            disconnectFrom(appSocket);
                            keepReading = false;
                            break;
                    }
                } catch (ClassNotFoundException | IOException ex) {
                    print("<< [Server]: Making a forced disconnection >>");
                    disconnectFrom(appSocket);
                    keepReading = false;
                    break;
                }
            }
        });
        commandThread.start();
        
        String[] playerOptions = {PLAYER_NET_OP, PLAYER_AI_OP};
        String selectedOption = "no option"; // deffault
        int responsePlayerIndex = askClient(appSocket, "which player do you want?", playerOptions);
        if(responsePlayerIndex >= 0) {
            selectedOption = playerOptions[responsePlayerIndex];
        }
        
        Player player1 = null, player2 = null;
        switch(selectedOption) {
            case PLAYER_AI_OP:
                print("AI game");
                String[] colorOptions = {COLOR_RED_OP, COLOR_BLUE_OP};
                int responseColorIndex = askClient(appSocket, "choose your color:", colorOptions);
                if(responseColorIndex >= 0 && colorOptions[responseColorIndex].equals(COLOR_BLUE_OP)){
                    print("chose blue");
                    player1 = new PlayerAi("red");
                    player2 = createPlayerNet("blue", appSocket);
                }else {
                    print("chose red");
                    player1 = createPlayerNet("red", appSocket);
                    player2 = new PlayerAi("blue");
                }
                break;
            case PLAYER_NET_OP:
                synchronized(this) {
                    if(waitingGuest != null){
                        player1 = createPlayerNet("red", waitingGuest);
                        player2 = createPlayerNet("blue", appSocket);
                        waitingGuest = null;
                    }else {
                        waitingGuest = appSocket;
                    }
                }
                break;
            default:
                disconnectFrom(appSocket);
//                try {
//                    appSocket.sendMessage(new Message(Message.DISCONNECT));
//                    appSocket.close();
//                    return;
//                } catch (IOException ex) {
//                    System.out.println("<< [Server] -> couldn't send the message (disconnect) >>");
//                }
                break;
        }
        
        if(player1 != null && player2 != null) {
            Game game = new Game(player1, player2);
            synchronized(gameList) { // game list is a global variable that can be used by many threads
                gameList.add(game);
            }
            
            // thread for playing the game in the background
            Thread gameThread = new Thread(() -> {
                print("start a new game");
                game.start();
            });
            print("\n---------\n");

            gameThread.start();
        }

    }
    
    /**
     * dissconnect from a client and update the playerNetList and the gameList
     * @param socket the socket to the client
     */
    private void disconnectFrom(AppSocket socket) {
        print("disconnect from: "+socket.toString());
        
        try {
            socket.sendMessage(new Message(Message.DISCONNECT));
        } catch (IOException ex) {
            // probably already disconnected - don't do anyting
        }
        
        synchronized(this) {
            if(waitingGuest == socket)
                waitingGuest = null;
        }
        
        // find the match player
        PlayerNet matchPlayer = null;
        for(PlayerNet player : playerNetList) {
            if(player.getAppSocket() == socket){
                matchPlayer = player;
                break;
            }
        }
        // find the match game
        Game matchGame = null;
        for(Game game : gameList) {
            if(game.getPlayer1() == matchPlayer || game.getPlayer2() == matchPlayer){
                matchGame = game;
                break;
            }
        }
        // stop the game
        if(matchGame != null)
            matchGame.gameOver(null, false);
        // remove the game and player from the ArrayList
        synchronized(playerNetList) {
            playerNetList.remove(matchPlayer);
        }
        synchronized(gameList) {
            gameList.remove(matchGame);
        }
        
        synchronized(socket) {
            socket.close();
        }
    }
    
    /**
     * send a request to the client to choose an option from a specific list.
     * after the client choose an option the choice is sent back to the server and the result will be returned here.
     * @param client the client's socket that the request will be sent to.
     * @param question content of the request.
     * @param options options to choose.
     * @return the index of the answer from the options array
     */
    private int askClient(AppSocket client, String question, String[] options) {
        try {
            // asking for an option:
            client.sendMessage(new Message( Message.Q_SELECT_OPTION, question, options ));
            
            // getting an option:
            Message response = client.readMessage();
            if(response.getSubject().equals(Message.OPTION) && response.getNumber() >= 0)
                return response.getNumber();
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("<< [Server] -> couldn't get an option from the client >>");
            ex.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * print and display the text to the JFrame window
     * @param str the text to display
     */
    private static void print(String str){
        System.out.println(str);
        infoArea.append("\n"+str);
        
        infoArea.setCaretPosition(infoArea.getDocument().getLength()); // for automatically scrolling down
    }
    
}
package default_package;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class Client {
    
    public static View view;
    public static AppSocket socket;
    
    public int waitingBoard; // index of the board that is waiting for direction to be detected
    public static String name;
//    private int minimxTime;
    
    public static void main(String[] args){
        Client client = new Client();
        client.start();
    }
    
    public Client(){
        this.socket = null;
        
        this.view = new View(this);
        view.setup();
        view.print("waiting for connection...");
        this.waitingBoard = 0;
        
        this.name = null;
    }
    
    /**
     * start the client, connect to the server and listen to requests
     */
    public void start(){
        connectToServer();
        if(socket != null){ // if the server was connected successfully
            view.print("wait for the your opponent...");
            
            Message message;
            while(true){
                try {
                    message = socket.readMessage();
                    System.out.println("# "+message.getSubject());
                    
                    switch(message.getSubject()){
                        case Message.Q_SELECT_OPTION:
                            System.out.println(message.toString());
                            int answerIndx = view.ask(message.getText(), message.getOptions());
                            System.out.println("answer: "+answerIndx);
                            
                            try {
                                socket.sendMessage(new Message(Message.OPTION, answerIndx));
                            } catch (IOException ex) {
                                System.out.println("<< [Client] -> cuoldn't send message (option) >>");
                                
                                view.print("Server disconnected, the game will be closed");
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException ex1) {
                                    System.out.println("sleep was interupted");
                                    ex.printStackTrace();
                                }
                                exit();
//                                ex.printStackTrace();
                            }
                            break;
                        case Message.NEW_GAME:
                            name = message.getText();
                            view.setup(message.getTools(), message.getcolors(), View.DARK_MODE);
                            view.setTitle("client ("+name+")");
                            view.print("wait for your turn...");
                            view.disableBoardButtons();
                            break;
                        case Message.GAME_OVER:
                            view.print("game over, "+message.getText()+" won");
                            view.disableBoardButtons();
                            view.disableDirection();
                            view.showWinner(message.getWinList());
                            
                            try {
                                Thread.sleep(4000);
                            } catch (InterruptedException ex) {
                                System.out.println("<< [Client] -> error while sleeping >>");
                                ex.printStackTrace();
                            }
                            
                            Thread counterThread = new Thread(() -> {
                                int countTime = 10;
                                for(int i=countTime; i>=0; i--){
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException ex) {
                                        System.out.println("sleep was interupted");
                                        ex.printStackTrace();
                                    }
                                    view.print("start again in "+i);
                                }
                                try {
                                    socket.sendMessage(new Message(Message.START_AGAIN));
                                } catch (IOException ex) {
                                    System.out.println("cuoldn't send message (start again)");
                                    
                                    view.print("Server disconnected, the game will be closed");
                                    try {
                                        Thread.sleep(5000);
                                    } catch (InterruptedException ex1) {
                                        System.out.println("sleep was interupted");
                                        ex.printStackTrace();
                                    }
                                    exit();
//                                    ex.printStackTrace();
                                }
                            });
                            counterThread.start();
                            break;
                        case Message.TECH_WIN:
                            view.print("you won, other player left");
                            
                            view.disableBoardButtons();
                            view.disableDirection();
                            break;
                        case Message.MAKE_MOVE:
                            view.print("your turn");
                            view.enableBoardButtons();
                            break;
                        case Message.WAIT_FOR_TURN:
                            view.print("wait for your turn...");
                            view.disableBoardButtons();
                            break;
                        case Message.UPDATE_BOARD:
                            view.updateBoard(message.getTools(), message.getcolors(), new Location(0, 0), new Location(5, 5));
                            break;
                        case Message.OTHER_PLAYER_LEFT:
                            view.print("other player left, the game will be closed");
                            view.popUp("other player left, you won the game");
                            view.disableBoardButtons();
                            view.disableDirection();
                            
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException ex) {
                                System.out.println("<< [Client] -> error while sleeping >>");
                                ex.printStackTrace();
                            }
                            
                            exit();
                            break;
                        case Message.ERROR:
                            System.out.println("# ("+message.getText()+")");
                            view.print("error: "+message.getText());
                            break;
                        case Message.DISCONNECT:
                            view.print("server disconnected, the game will be closed");
                            view.disableBoardButtons();
                            view.disableDirection();
                            
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException ex) {
                                System.out.println("<< [Client] -> error while sleeping >>");
                                ex.printStackTrace();
                            }
                            
                            exit();
                            break;
                        default:
                            System.out.println("<< Controller -> couldn't understand what the server sended >>");
                    }
                    
                } catch (ClassNotFoundException | IOException ex) {
                    System.out.println("<< Controller -> error: couldn't get the message >>");
                    
                    view.print("Server disconnected, the game will be closed");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex1) {
                        System.out.println("sleep was interupted");
                        ex.printStackTrace();
                    }
                    exit();
//                    ex.printStackTrace();
                    break;
                }
            }
        }
    }
    
    /**
     * connect to the server
     */
    public static void connectToServer(){
        String addressIp;
        int addressPort;
        socket = null;
        String question = "write the address of the server (ip:port)";
        while(socket == null) {
            String localhost = "localhost";
            try {
                localhost = InetAddress.getLocalHost().getHostAddress();
//                localhost = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName())[1].getHostAddress();
            } catch (UnknownHostException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
            String address = view.ask(question, localhost+":3000");
            if(address == null) {
                exit();
                
            }
            
            try {
//                addressIp = InetAddress.getLocalHost();
                addressIp = address.substring(0, address.indexOf(':'));
                
                String portStr = address.substring(address.indexOf(':') + 1);
                addressPort = Integer.parseInt(portStr);
            }catch (Exception e) {
                question = "address in not valid, try again (ip:port)";
                continue;
            }
            
            try {
                Socket msgServerSocket = new Socket(addressIp, addressPort); // for messages running on the game (socker.readMessage())
                Socket cmdServerSocket = new Socket(addressIp, addressPort); // for commands for actions out of the game (socker.readCommand())

                socket = new AppSocket(msgServerSocket, cmdServerSocket);
                view.print("connected");
            } catch (IOException ex) {
                System.out.println("<< Controller -> error: couldn't connect to the server >>");
                ex.printStackTrace();

                view.print("error: couldn't connect, game will be closed");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex1) {
                    System.out.println("sleep was interupted");
                    ex.printStackTrace();
                }
                view.close();
                System.exit(0);
            }
        }
    }
    
    /**
     * not supported yet
     */
    public static void startNewGame() {
//         view pressed new game
    }
    
    
//    public void makeRandomMove(){
//        
//    }
    
    /**
     * not supported yet
     * @param command 
     */
    public void commandPressed(String command){ // the buttons in the north area
//        switch(command){
//            case "New Game":
//                startNewGame();
//                break;
//            case "AI Move":
//                System.out.println("\n--- MiniMax Test ---\n");
//                
//                Action minimaxAction = model.getMinimaxAction(new State(currentState), State.LOSE_SCORE, State.WIN_SCORE, 0, Model.MINIMAX_DEPTH);
////                Action minimaxAction = model.getMinimaxInTime(new State(currentState), minimxTime);
//                
//                System.out.println("\n--- MiniMax done ---\n");
//                System.out.println("");
//                System.out.println("best action is:\n"+minimaxAction);
//                System.out.println("");
//                
//                if(minimaxAction.getPut() != null || minimaxAction.getMove() != null){
//                    Put mPut = minimaxAction.getPut();
//                    boolean thereIsWinner = putTool(mPut.getLocation());
//                    if(thereIsWinner)
//                        return;
//                    Move mMove = minimaxAction.getMove();
//                    if(mMove.getDirection() != Consts.NO_DIRECTION)
//                        makeBoardAction(mMove.getBoard(), mMove.getDirection());
//                }
//                else
//                    System.out.println("<< Controller->AI move: i got null from minimax >>");
//                
//                break;
//            case "Random Move":
//                makeRandomMove();                
//                break;
//            default:
//                System.out.println("controller: didn't understand your command");
//        }
    }
    
    /**
     * not supported yet
     * @param locateToSet 
     */
    public void putToolPressed(Location locateToSet){
        waitingBoard = 0; // automaticly
        
        Message message = new Message(Message.PUT, locateToSet);
        try {
            socket.sendMessage(message);
        } catch (IOException ex) {
            System.out.println("<< Controller -> error: couldn't send the message (put) >>");
            ex.printStackTrace();
        }
    }

    /**
     * not supported yet
     * @param boardIndx 
     */
    public void movePressed(int boardIndx){
        System.out.println("move pressed (1)");
        view.enableDirection();
        
        waitingBoard = boardIndx;
        view.enableDirection();
    }
    
    /**
     * not supported yet
     * @param command 
     */
    public void directionPressed(String command) { // button pressed to show where to move (after we decide who to move)
        System.out.println("direction pressed - "+command);
        String[] commandSp = command.split(" ");
        char direction = '0';
        switch(command){
            case "move left":
                direction = Consts.LEFT;
                break;
            case "move right":
                direction = Consts.RIGHT;
                break;
            case "move up":
                direction = Consts.UP;
                break;
            case "move down":
                direction = Consts.DOWN;
                break;
            case "rotate left":
                direction = Consts.R_LEFT;
                break;
            case "rotate right":
                direction = Consts.R_RIGHT;
                break;
            case "no direction":
                direction = Consts.NO_DIRECTION;
                break;
            default:
                System.out.println("<< controller: didn't get an appropriate direction >>");
                return;
        }
        
        System.out.println("direction - "+direction);
        view.disableDirection();
        
        Message message = new Message(Message.ACTION, waitingBoard, direction);
        try {
            System.out.println("send message:");
            System.out.println(message.toString());   
            socket.sendMessage(message);
            
            waitingBoard = 0;
        } catch (IOException ex) {
            System.out.println("<< Controller -> error: couldn't send the message (action) >>");
            ex.printStackTrace();
        }
//        makeBoardAction(waitingBoard, direction);
    }

    /**
     * not supported yet
     * @param timeNum 
     */
    public void changeAiTime(int timeNum) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * disconnect from the server and stops the app proccess
     */
    public static void exit() {
        try {
            socket.sendCommand(new Message(Message.DISCONNECT));
        } catch (Exception e) {
            System.out.println("<< [Client] -> couldn't send the message to the server (disconnect) >>");
//            ex.printStackTrace();
        }
        
        try {
            if(socket != null) {
                socket.close();
            }
        }catch(Exception e) {
            System.out.println("Couldn't close the socket");
            e.printStackTrace();
        }
        
        if(view != null) {
            view.close();
        }
        
        System.exit(0);
    }
    
}
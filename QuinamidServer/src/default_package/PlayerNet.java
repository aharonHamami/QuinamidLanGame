package default_package;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class PlayerNet extends Player {

    private AppSocket socket;
    private boolean putMade;

    public PlayerNet(String name, AppSocket socket) {
        super(name);
        this.socket = socket;
        this.putMade = false;
    }
    
    /**
     * update the player that the game has started
     * @param state current state of the game
     */
    @Override
    public void startGame(State initState){
        putMade = false;
        try {
            socket.sendMessage(new Message(Message.NEW_GAME, initState.getTools(), initState.getColors(), super.name));
        } catch (IOException ex) {
            System.out.println("<< PlayerNet -> couldn't send the message (new game) >>");
            ex.printStackTrace();
        }
    }
    
    /**
     * make a move on a given state
     * @param state current state of the game
     * @return true if the move has been made successfully, false otherwise
     */
    @Override
    public boolean makeMove(State state) {
        ArrayList<Location> winList = null;
        
        try {
            Message message = new Message(Message.MAKE_MOVE);
            socket.sendMessage(message);
        } catch (IOException ex) {
            System.out.println("<< playerNet -> error: couldn't send the message ('your turn') >>");
//            ex.printStackTrace();
            // the player is out of contact, so the opposite player wins
            return false;
        }
        
        // get a put
        while(true){
            Message locationMsg = null;
            try {
                // wait for location (for putting a tool)
                locationMsg = socket.readMessage(); // location - put location
                if(!locationMsg.getSubject().equals(Message.PUT) || locationMsg.getLocation() == null){
                    Message message = new Message(Message.ERROR, "put a tool");
                    socket.sendMessage(message);
                    continue;
                }
            } catch (IOException | ClassNotFoundException ex) {
                System.out.println("<< [playerNet] -> error: couldn't get a location >>");
//                ex.printStackTrace();
                return false;
            }

            // putTool()
            String error = putTool(state, locationMsg.getLocation());
            if(error != null){
                try {
                    socket.sendMessage(new Message(Message.ERROR, error));
                } catch (IOException ex) {
                    System.out.println("<< playerNet -> error: couldn't send the message (error) >>");
                }
                continue;
            }
            
            break;
        }
        
        try {
            // send an update of the board
            Message message = new Message(Message.UPDATE_BOARD, state.getTools(), state.getColors());
            socket.sendMessage(message);
        } catch (IOException ex) {
            System.out.println("<< playerNet -> error: couldn't send the message (board) >>");
            ex.printStackTrace();
        }
        
        winList = state.checkWin(new Location(0, 0), new Location(5, 5));
        if(winList != null) // we the put caused winning we can stop
            return true;
        
        // getting board index/color and direction
        while(true){
            Message actionMsg = null;
            try {
                // wait for an action
                actionMsg = socket.readMessage(); // number - borad color, char - direction
                if(!actionMsg.getSubject().equals(Message.ACTION) || actionMsg.getChar() == '\u0000'){
                    Message message = new Message(Message.ERROR, "make an action");
                    socket.sendMessage(message);
                    continue;
                }
            } catch (IOException | ClassNotFoundException ex) {
                System.out.println("<< playerNet -> error: prolem with getting action >>");
//                ex.printStackTrace();
                return false;
            }

            // makeBoardAction()
            String error = makeBoardAction(state, actionMsg.getNumber(), actionMsg.getChar());
            if(error != null){
                try {
                    socket.sendMessage(new Message(Message.ERROR, error));
                } catch (IOException ex) {
                    System.out.println("<< playerNet -> error: couldn't send the message (error) >>");
                }
                continue;
            }
            break;
        }
        
        // send an update of the board
        try {
            // send an update of the board
            Message message = new Message(Message.UPDATE_BOARD, state.getTools(), state.getColors());
            socket.sendMessage(message);
        } catch (IOException ex) {
            System.out.println("<< playerNet -> error: couldn't send the message (colors) >>");
            ex.printStackTrace();
        }
        
        try {
            // close buttons
            Message message = new Message(Message.WAIT_FOR_TURN);
            socket.sendMessage(message);
        } catch (IOException ex) {
            System.out.println("<< playerNet -> error: couldn't send the message ('enemy turn') >>");
            ex.printStackTrace();
        }
        
        winList = state.checkWin(new Location(0, 0), new Location(5, 5));
        if(winList != null)
            return true;
        
        return true;
    }
    
    /**
     * put a tool on a given state
     * @param currentState current state of the game.
     * @param locateToSet location where to put the tool on.
     * @return error messaege if there is an error, null otherwise.
     */
    private String putTool(State currentState, Location locateToSet) { // button pressed to set tool
        if(putMade){
            System.out.println("<< controller: put already made, make a move or press <space> to continue >>");
            return "make a move";
        }
//        System.out.println("controller: put tool in: "+locateToSet.toString());
        
        if(currentState.isPutPossible(locateToSet))
            currentState.doPut(locateToSet);
        else{
            System.out.println("<< PlayerNet -> error: you can't do that put >>");
            return "can't do that move";
        }
        
        putMade = true;
        
        return null;
    }
    
    // return: error message.
    /**
     * make an action on a board of a given state
     * @param currentState current state of the game.
     * @param boardIndx the board index/hight on the state.
     * @param direction the direction where to move the board to.
     * @return error messaege if there is an error, null otherwise.
     */
    private String makeBoardAction(State currentState, int boardIndx, char direction){ // action = rotate/move
        if(!putMade){
            System.out.println("<< controller: you didn't do put yet >>");
            return "first put a tool";
        }
//        System.out.println("controller: making action: "+direction+" for: "+boardIndx);
        Board currentBoard = currentState.getBoards()[boardIndx];
        if(currentState.isActionPossible(boardIndx, direction))
            currentState.doAction(currentBoard, direction);
        else{
            System.out.println("<< controller: you can't do that move >>");
            return "can't do that move";
        }
        
        putMade = false;
        return null;
    }
    
    /**
     * update the player about the new state of the game
     * @param state current state of the game
     */
    @Override
    public void updateState(State currentState){
        try {
            socket.sendMessage(new Message(Message.UPDATE_BOARD, currentState.getTools(), currentState.getColors()));
        } catch (IOException ex) {
            System.out.println("<< PlayerNet -> couldn't update the player for the changes >>");
            // don't do anything for now - it's not critic
//            ex.printStackTrace();
        }
    }
    
    /**
     * update the player that the game is over
     * @param name who won the game
     * @param winList win series
     */
    @Override
    public void gameOver(String name, ArrayList<Location> winList){
        if(!socket.isClosed()) {
            if(!winList.isEmpty()){
                try {
                    socket.sendMessage(new Message(Message.GAME_OVER, name, winList));
                } catch (IOException ex) {
                    System.out.println("<< PlayerNet -> couldn't send the message >>");
                    ex.printStackTrace();
                }
            }else {
                try {
                    socket.sendMessage(new Message(Message.TECH_WIN, name));
                } catch (IOException ex) {
                    System.out.println("<< PlayerNet -> couldn't send the message (tech win) >>");
//                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * check if the player wants to start again
     * @return true if the player wants to start again
     */
    @Override
    public boolean checkStartAgain() {
        
//        // ask to start again:
//        try {
//            socket.sendMessage(new Message(Message.Q_START_AGAIN));
//        } catch (IOException ex) {
//            System.out.println("<< PlayerNet -> couldn't send the message >>");
//            ex.printStackTrace();
//            return false;
//        }
        
        // reading response:
        if(!socket.isClosed()) {
            try {
                Message message = socket.readMessage();
                if(message.getSubject().equals(Message.START_AGAIN))
                    return true;
                else
                    return false;
            } catch (ClassNotFoundException | IOException ex) {
                System.out.println("<< [Player Net] -> couldn't get message (start again) >>");
    //            ex.printStackTrace();
            }
        }
        
        return false;
    }
    
    /**
     * update the player that the antagonist left the game
     */
    @Override
    public void otherPlayerLeft() {
        if(!socket.isClosed()){
            try {
                socket.sendMessage(new Message(Message.OTHER_PLAYER_LEFT));
            } catch (IOException ex) {
                System.out.println("<< PlayerNet -> couldn't send the message (other player left) >>");
//                ex.printStackTrace();
            }
        }
    }
    
    public AppSocket getAppSocket() {
        return socket;
    }
    
}

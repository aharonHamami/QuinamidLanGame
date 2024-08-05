package default_package;

import java.util.ArrayList;

public abstract class Player {
//    private static final String lettersString = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghij"
//    protected int id;
    protected String name;
    
    public Player(String name){
//        this.id = 
        this.name = name;
    }

    /**
     * update the player that the game has started
     * @param state current state of the game
     */
    public abstract void startGame(State state);

    /**
     * make a move on a given state
     * @param state current state of the game
     * @return true if the move has been made successfully, false otherwise
     */
    public abstract boolean makeMove(State state);
    
    /**
     * update the player about the new state of the game
     * @param state current state of the game
     */
    public abstract void updateState(State state);

    /**
     * update the player that the game is over
     * @param name who won the game
     * @param winList win series
     */
    public abstract void gameOver(String name, ArrayList<Location> winList);
    
    /**
     * update the player that the antagonist left the game
     */
    public abstract void otherPlayerLeft();
    
    /**
     * check if the player wants to start again
     * @return true if the player wants to start again
     */
    public abstract boolean checkStartAgain();
    
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Player{" + "name=" + name + '}';
    }

}

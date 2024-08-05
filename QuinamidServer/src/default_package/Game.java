package default_package;

import java.util.ArrayList;
import javax.swing.JOptionPane;

public class Game {
    private Player p1;
    private Player p2;
    private Player currentPlayer; // the player that currently playing
    private State state; // the current state of the board of the game
    private boolean gameOver;
    
    public Game(Player p1, Player p2){
        this.p1 = p1;
        this.p2 = p2;
        this.state = new State();
    }
    
    /**
     * start the game
     */
    public void start(){
        System.out.println(" - starting a game - ");
        
        boolean startGame = true;
        while(startGame){
            this.state.setup();
            // send to both: setup
            p1.startGame(state);
            p2.startGame(state);
            
            System.out.println(state.toString());

            // here we start the game:
            currentPlayer = p1; // the player who start
            ArrayList<Location> winList = null;
            gameOver = false;
            boolean success = true; // did the game go successfully? / did someone dissconnect?
            while(!gameOver){
                System.out.println("[game]: current player: "+currentPlayer);
                success = currentPlayer.makeMove(state);
                p1.updateState(state);
                p2.updateState(state);
                winList = state.checkWin(new Location(0, 0), new Location(5, 5));
                if(winList != null || !success){ // we have a series of win - there is a winner
                    break;
                }
                switchPlayer();
            }
            System.out.println("[after the game]: current player: "+currentPlayer);
            startGame = gameOver(winList, success);
            System.out.println("start again = "+startGame);
        }
        
        System.out.println("# A game was disposed");
    }
    
    /**
     * ends the game
     * @param winList - the win series, null if there is no win series.
     * @param successfull - did the game end as expected?
     * @return true if the players want to start again, false otherwise.
     */
    public boolean gameOver(ArrayList<Location> winList, boolean successfull) {
        gameOver = true;
        boolean startAgain = false;
        
        System.out.println(" -- game over -- ");
        
        // if winList is empty its a technical win
        System.out.println("successfull? :"+successfull);
        System.out.println("current player: "+currentPlayer);
        Player winner;
        if(winList != null && winList.size() > 0) {
            // checking the color on one of the tools that made the win series
            if(state.getTools()[winList.get(0).getY()][winList.get(0).getX()] == Consts.RED)
                winner = p1;
            else
                winner = p2;
        } else {
            winner = (successfull ? currentPlayer : getAntagonist(currentPlayer));
        }
        ArrayList<Location> winSeries = ((successfull && winList != null) ? winList : new ArrayList<Location>());
            
        p1.gameOver(winner.getName(), winSeries);
        p2.gameOver(winner.getName(), winSeries);

        System.out.println("asking for a new game");

        boolean startAgain1 = false, startAgain2 = false;
        if(successfull) {
            startAgain1 = p1.checkStartAgain();
            startAgain2 = p2.checkStartAgain();
        }

        if(!startAgain1)
            p2.otherPlayerLeft();
        if(!startAgain2)
            p1.otherPlayerLeft();

        startAgain = startAgain1 && startAgain2;
        
        return startAgain;
    }
    
    /**
     * switch the current player (currentPlayer) between p1 and p2 and update the state
     */
    public void switchPlayer(){
        state.swapTurn();
        currentPlayer = (currentPlayer == p1 ? p2 : p1);
    }
    
    /**
     * get the antagonist of the specified player
     * @param player - the player
     * @return the antagonist of the player
     */
    public Player getAntagonist(Player player) {
        return (player == p1 ? p2 : p1);
    }
    
    public Player getPlayer1() {
        return p1;
    }

    public Player getPlayer2() {
        return p2;
    }

    public boolean isGameOver() {
        return gameOver;
    }

}

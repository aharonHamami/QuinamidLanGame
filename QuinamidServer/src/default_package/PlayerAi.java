package default_package;

import java.util.ArrayList;

public class PlayerAi extends Player{
    
    private Model model;
    private int minimaxTime;

    public PlayerAi(String name) {
        super(name);
        model = new Model();
        
        minimaxTime = 10;
    }
    
    /**
     * update the player that the game has started
     * @param state current state of the game
     */
    @Override
    public void startGame(State state) {} // not doing anything
   
    /**
     * make a move on a given state
     * @param state current state of the game
     * @return true if the move has been made successfully, false otherwise
     */
    @Override
    public boolean makeMove(State state) {
        makeAiMove(state);
//        makeRandomMove(state);

        return true;
    }
    
    /**
     * making a minimax move on a given state
     * @param state the current state
     * @return win series if the minimax won the game
     */
    private ArrayList<Location> makeAiMove(State state) {
        ArrayList<Location> winList;
        
        System.out.println("--- MiniMax start ---");

//        Action minimaxAction = model.getMinimaxAction(new State(state), State.LOSE_SCORE, State.WIN_SCORE, 0, Model.MINIMAX_DEPTH);
        Action minimaxAction = model.getMinimaxInTime(new State(state), minimaxTime);

        System.out.println("--- MiniMax done ---");
//        System.out.println("");
//        System.out.println("best action is:\n"+minimaxAction);
//        System.out.println("");

        if(minimaxAction.getPut() != null || minimaxAction.getMove() != null){
            Put mPut = minimaxAction.getPut();
            putTool(state, mPut.getLocation());
            winList = state.checkWin(new Location(0, 0), new Location(5, 5));
            if(winList != null)
                return winList;
            
            Move mMove = minimaxAction.getMove();
            if(mMove.getDirection() != Consts.NO_DIRECTION)
                makeBoardAction(state, mMove.getBoard(), mMove.getDirection());
            winList = state.checkWin(new Location(0, 0), new Location(5, 5));
            if(winList != null)
                return winList;
        }
        else
            System.out.println("<< Controller->AI move: i got null from minimax >>");
        
        return null;
    }
    
    /**
     * making a random move on a given state
     * @param state the current state
     * @return win series if the random move won the game
     */
    private ArrayList<Location> makeRandomMove(State state){
        ArrayList<Location> winList;
        
        // put:
        Put randPut = state.getActionManager().getRandomPut();
        
        putTool(state, randPut.getLocation());
        winList = state.checkWin(new Location(0, 0), new Location(5, 5));
        if(winList != null)
            return winList;
        
        // move:
        Move randMove = state.getActionManager().getRandomMove();
        
        if(randMove.getDirection() != Consts.NO_DIRECTION)
            makeBoardAction(state, randMove.getBoard(), randMove.getDirection());
        winList = state.checkWin(new Location(0, 0), new Location(5, 5));
        if(winList != null)
            return winList;
        
        return null;
    }
    
    /**
     * put a tool on a given state
     * @param currentState current state of the game.
     * @param locateToSet location to put the tool on.
     */
    private void putTool(State currentState, Location locateToSet) { // button pressed to set tool
        System.out.println("controller: put tool in: "+locateToSet.toString());
        // move:
        if(currentState.isPutPossible(locateToSet))
            currentState.doPut(locateToSet);
        else{
            System.out.println("<< [controller]: error: playerAi is making an immpossible put >>");
        }
    }
    
    /**
     * make an action on a board of a given state
     * @param currentState current state of the game.
     * @param boardIndx the board index/hight on the state.
     * @param direction the direction where to move the board to.
     */
    private void makeBoardAction(State currentState, int boardIndx, char direction){ // action = rotate/move
        System.out.println("controller: making action: "+direction+" for: "+boardIndx);
        Board currentBoard = currentState.getBoards()[boardIndx];
        if(currentState.isActionPossible(boardIndx, direction))
            currentState.doAction(currentBoard, direction);
        else{
            System.out.println("<< [controller]: error: playerAi is makin an immpossible action>>");
        }
    }
    
    /**
     * update the player about the new state of the game
     * @param state current state of the game
     */
    @Override
    public void updateState(State state) {} // not doing anything

    /**
     * update the player that the game is over
     * @param name who won the game
     * @param winList win series
     */
    @Override
    public void gameOver(String name, ArrayList<Location> winList) {} // not doing anything
    
    /**
     * update the player that the antagonist left the game
     */
    @Override
    public void otherPlayerLeft() {} // not doing anything

    /**
     * check if the player wants to start again
     * @return true if the player wants to start again
     */
    @Override
    public boolean checkStartAgain() {
        return true;
    }
    
}

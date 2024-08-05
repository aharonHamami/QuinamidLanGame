package default_package;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class Message implements Serializable{
    // server types:
    public static final String ERROR = "error";
    public static final String NEW_GAME = "new game";
    public static final String GAME_OVER = "game over";
    public static final String TECH_WIN = "technical win";
    public static final String MAKE_MOVE = "make move";
    public static final String WAIT_FOR_TURN = "wait for your turn";
    public static final String UPDATE_BOARD = "board";
    public static final String OTHER_PLAYER_LEFT = "the other player left";
    public static final String Q_START_AGAIN = "question to satrt again";
    public static final String Q_SELECT_OPTION = "question to select option";
    
    // client types:
    public static final String PUT = "put";
    public static final String ACTION = "action";
    public static final String START_AGAIN = "start again";
    public static final String OPTION = "selected option";
    
    // common types:
    public static final String DISCONNECT = "disconnect";
    
    private String subject;
    
    private String text;
    private Location location;
    private String[] options;
    private char[][] tools;
    private int[][] colors;
    private char ch;
    private int number;
    private ArrayList<Location> winList;
    
    // basic constructor
    public Message(String subject){
        this.subject = subject;
    }
    
    /**
     * Creates an instance of Message for a simple text
     * @param subject
     * @param text 
     */
    public Message(String subject, String text){
        this.subject = subject;
        this.text = text;
    }
    
    /**
     * Creates an instance of Message for location
     * @param subject
     * @param location 
     */
    public Message(String subject, Location location){
        this.subject = subject;
        this.location = new Location(location);
    }
    
    /**
     * Creates an instance of Message for a board
     * @param subject
     * @param tools tools of the board
     * @param colors colors of the board
     */
    public Message(String subject, char[][] tools, int[][] colors){
        this.subject = subject;
        
        char[][] copyTools = new char[tools.length][tools[0].length];
        for (int i = 0; i < copyTools.length; i++) {
            for (int j = 0; j < copyTools[0].length; j++) {
                copyTools[i][j] = tools[i][j];
            }
        }
        this.tools = copyTools;
        
        int[][] copyColors = new int[colors.length][colors[0].length];
        for (int i = 0; i < copyColors.length; i++) {
            for (int j = 0; j < copyColors[0].length; j++) {
                copyColors[i][j] = colors[i][j];
            }
        }
        this.colors = copyColors;
    }
    
    /**
     * Creates an instance of Message for a new game and settings for starting
     * @param subject
     * @param tools the tools of the board of the new game
     * @param colors the color of the board of the new game
     * @param name the name of the client's player
     */
    Message(String subject, char[][] tools, int[][] colors, String name) {
        this.subject = subject;
        
        char[][] copyTools = new char[tools.length][tools[0].length];
        for (int i = 0; i < copyTools.length; i++) {
            for (int j = 0; j < copyTools[0].length; j++) {
                copyTools[i][j] = tools[i][j];
            }
        }
        this.tools = copyTools;
        
        int[][] copyColors = new int[colors.length][colors[0].length];
        for (int i = 0; i < copyColors.length; i++) {
            for (int j = 0; j < copyColors[0].length; j++) {
                copyColors[i][j] = colors[i][j];
            }
        }
        this.colors = copyColors;
        
        this.text = name;
    }

    /**
     * Creates an instance of Message for an action
     * @param subject
     * @param board the board that the action applies to
     * @param direction the direction to move the board
     */
    public Message(String subject, int board, char direction){
        this.subject = subject;
        this.number = board;
        this.ch = direction;
    }
    
    /**
     * Creates an instance of Message for ending a game
     * @param subject
     * @param name name of the player that won
     * @param winList win series, null if there is no win series
     */
    public Message(String subject, String name, ArrayList<Location> winList){
        this.subject = subject;
        this.text = name;
        this.winList = winList;
    }
    
    /**
     * Creates an instance of Message for question for a client with options to choose from
     * @param subject
     * @param text the content of the question
     * @param options options for the client to choose from
     */
    public Message(String subject, String text, String[] options) {
        this.subject = subject;
        this.text = text;
        this.options = options;
    }
    
    /**
     * Creates an instance of Message for an option that the client chose from a given list
     * @param subject
     * @param option the index of the option from the array that was given to the client
     */
    public Message(String subject, int option) {
        this.subject = subject;
        this.number = option;
    }

    public String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }
    
    public String[] getOptions() {
        return options;
    }
    
    public char[][] getTools(){
        return tools;
    }
    
    public int[][] getcolors(){
        return colors;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public char getChar() {
        return ch;
    }
    
    public int getNumber() {
        return number;
    }
    
    public ArrayList<Location> getWinList() {
        return winList;
    }
    
    // compare to

    @Override
    public String toString() {
        String toolsTable = "";
        if(tools != null){
            for (int i = 0; i < tools.length; i++) {
                toolsTable += "\t"+Arrays.toString(tools[i])+"\n";
            }
        }
        
        String colorsTable = "";
        if(colors != null){
            for (int i = 0; i < colors.length; i++) {
                colorsTable += "\t"+Arrays.toString(colors[i])+"\n";
            }
        }
        
        return "Message {" 
                + "\n\tsubject=" + subject 
                + ",\n\ttext=" + text 
                + ",\n\tlocation=" + location 
                + ",\n\toptions=" + Arrays.toString(options)
                + ",\n\ttools={\n" + toolsTable + "\t}"
                + ",\n\tcolors={\n" + colorsTable + "\t}"
                + ",\n\tch=" + ch 
                + ",\n\tnumber=" + number 
                + ",\n\twinList=" + (winList == null ? null : winList.toString()) 
                + "\n}";
    }

    
}

package default_package;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppSocket {
    private Socket msgSocket; // message socket
    private ObjectOutputStream msgOutput; // ouput stream for message 
    private ObjectInputStream msgInput; // input stream for message
    
    private Socket cmdSocket; // command socket
    private ObjectOutputStream cmdOutput; // ouput stream for command
    private ObjectInputStream cmdInput; // input stream for command
    
    public AppSocket(Socket msgSocket, Socket cmdSocket){
        this.msgSocket = msgSocket;
        try {
            this.msgOutput = new ObjectOutputStream(msgSocket.getOutputStream());
            this.msgInput = new ObjectInputStream(msgSocket.getInputStream());
        } catch (IOException ex) {
            System.out.println("appSocketServer: couldn't make input and output stream");
            ex.printStackTrace();
        }
        
        this.cmdSocket = cmdSocket;
        try {
            this.cmdOutput = new ObjectOutputStream(cmdSocket.getOutputStream());
            this.cmdInput = new ObjectInputStream(cmdSocket.getInputStream());
        } catch (IOException ex) {
            System.out.println("appSocketServer: couldn't make input and output stream");
            ex.printStackTrace();
        }
    }
    
    /**
     * send a message through the messaeg sokcet
     * @param msg message
     * @throws IOException 
     */
    public void sendMessage(Message msg) throws IOException{
        this.msgOutput.writeObject(msg);
        this.msgOutput.flush(); // send without waiting for the buffer
    }
    
    /**
     * send a message through the command sokcet
     * @param cmd
     * @throws IOException 
     */
    public void sendCommand(Message cmd) throws IOException{
        this.cmdOutput.writeObject(cmd);
        this.cmdOutput.flush(); // send without waiting for the buffer
    }
    
    /**
     * waiting for a message from the message socket
     * @return the message
     * @throws ClassNotFoundException
     * @throws IOException 
     */
    public Message readMessage() throws ClassNotFoundException, IOException {
        Message msg = (Message)(this.msgInput.readObject());
        return msg;
    }
    
    /**
     * waiting for a message from the command socket
     * @return the message
     * @throws ClassNotFoundException
     * @throws IOException 
     */
    public Message readCommand() throws ClassNotFoundException, IOException {
        Message cmd = (Message)(this.cmdInput.readObject());
        return cmd;
    }
    
    /**
     * close the message and command sockets
     */
    public void close() {
        try {
            // message closing:
            this.msgInput.close();
            this.msgOutput.close();
            this.msgSocket.close();
        } catch (IOException ex) {
            System.out.println("<<[AppSocket]->couldn't close message socket>>");
//            Logger.getLogger(AppSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            // command closing:
            this.cmdInput.close();
            this.cmdOutput.close();
            this.cmdSocket.close();
        } catch (IOException ex) {
            System.out.println("<<[AppSocket]->couldn't close command socket>>");
//            Logger.getLogger(AppSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * check if the messsage or command socket are close
     * @return 
     */
    public boolean isClosed() {
        return msgSocket.isClosed() || cmdSocket.isClosed();
    }
    
//    public boolean isAlive(){return true;} // needs to send a messsage to see if the client is still alive
}

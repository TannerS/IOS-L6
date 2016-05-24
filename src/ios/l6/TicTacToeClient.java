package ios.l6;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TicTacToeClient 
{

    public static void main(String[] args) 
    {
        // needed objects and variables
        Socket socket = null;
        ObjectOutputStream ob_out = null;
        ObjectInputStream ob_in = null;
        BoardMessage board = null;
        ErrorMessage error = null;
        Scanner input = null;
        Message message = null;
        byte row = -1;
        byte col = -1;
        // start program
        try
        {
            System.out.println();
            // open socket
            socket = new Socket("codebank.xyz", 38006); 
            // display message
            System.out.println("Now Connected To Server");
            System.out.println();
            // load output stream for objects
            ob_out = new ObjectOutputStream(socket.getOutputStream());
            // load input streams for objects
            ob_in = new ObjectInputStream(socket.getInputStream());
            // start loop to be able to play multiple times
            // send connect message to server with user name
            ob_out.writeObject(new ConnectMessage("TannerS"));
            // display message
            System.out.println("Sent User Information");
            System.out.println();
            // send commandmessage with new game command
            ob_out.writeObject(new CommandMessage(CommandMessage.Command.NEW_GAME));
            // display message
            System.out.println("New Game Starting");
            System.out.println();
            System.out.println("Enter -1 for row and col to surrender at any turn");
            System.out.println();
            // get board object
            board = (BoardMessage) ob_in.readObject();
            // show current board
            System.out.println(showBoard(board.getBoard()));
            // gscanner to get rows and cols
            input = new Scanner(System.in);
            // loop while game is going
            while(board.getStatus().equals(BoardMessage.Status.IN_PROGRESS) )
            {
                // get rows and col
                System.out.print("Enter Row: ");
                row = input.nextByte();
                System.out.print("Enter Col: ");
                col = input.nextByte();
                System.out.println();
                // if use decides to give up
                if(row == -1 && col == -1)
                {
                    // send surrender message
                    ob_out.writeObject(new CommandMessage(CommandMessage.Command.SURRENDER));
                    // get response
                    message = (Message) ob_in.readObject();
                    // find type
                    if(message.getType().equals(MessageType.ERROR))
                    {
                        // cast to proper object
                        error = (ErrorMessage) message;    
                        // display error message
                        System.out.println(error.getError());
                        System.out.println("Player surrendered");
                        // break loop
                        break;
                    }
                }
                // user wants to keep playing
                else
                {
                    // send move to server
                    ob_out.writeObject(new MoveMessage(row, col));
                    // get message back (parent class)
                    message = (Message) ob_in.readObject();
                    // return message is a board message
                    if(message.getType().equals(MessageType.BOARD))
                    {
                        // cast to proper object
                        board = (BoardMessage) message;
                        // board status indicates error
                        if(board.getStatus().equals(BoardMessage.Status.ERROR))
                        {
                            // error
                            System.out.println("An Error Has Occurred, Exiting Game");
                            // close main
                            break;
                        }
                        // no error occurred
                        else
                            // display game board
                            System.out.println(showBoard(board.getBoard())); 
                    }
                    // error has occurred
                    else if(message.getType().equals(MessageType.ERROR))
                    {
                        // cast to proper object
                        error = (ErrorMessage) message;    
                        // display error message
                        System.out.println(error.getError());
                        System.out.println();
                    }    
                }
            }
            
            
            //make sure game didn't end with error
            if(!(message.getType().equals(MessageType.ERROR)))
                // this point happens if the status of the board
                // changes from in-progress, to something else
                System.out.println(board.getStatus().toString());
            // close sockets
            socket.close();
            ob_out.close();
            ob_in.close();
        } 
        catch (IOException ex) 
        {
            System.out.println("PROBLEM WITH IO");
            Logger.getLogger(TicTacToeClient.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        catch (ClassNotFoundException ex) 
        {
            System.out.println("PROBLEM WITH CLASS");
            Logger.getLogger(TicTacToeClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    static String showBoard(byte[][] board)
    {
        String temp = "";
        
        for(int i = 0; i < 3; i++)
        {
            for(int j = 0; j < 3; j++)
                temp += board[i][j] + " ";
            temp += "\n";
        }
        return temp;
    }
}


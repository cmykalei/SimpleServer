import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.InetAddress;
/**
 * 
 * Paper:       COMPX204-24B
 * Assignment:  1 Introduction to Sockets
 * Task:        3b SimpleClient (Simple Network)
 * Name:        Kalei Esteves (u:/ke131)
 * ID:          1282608
 * 
 **/
/**
 * SimpleClient Class.
 * SimpleClient opens a Socket bound to the specified SimpleServer address
 * or DNS and port number.
 * Similar implementation to SimpleServer, but only uses one 'try-with-resources'
 * block to open a connection.
 * Please note: I've attempted to use threads! I've included commented lines 
 *              to stop the program before this, if necessary.
 * 
 * @see     SimpleServer
 */
public class SimpleClient {

    private static volatile boolean running = true; // Flag used in threads.
    /**
     * Static method main is the entry point to this program.
     * 
     * Note:    Args must be of length 2, and containing 
     *          a host address or dns and port number.                 
     * @param   args    Specifies the user input from the command-line.
     */  
    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("> Usage: java SimpleClient <host> <port>");
            System.exit(0);
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        System.out.println("\nEnter 'exit' to close the connection.\n");
        // Tries to connect using the specified host and port from args
        try (
            Socket socket = new Socket(host, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
        ) {
            out.println(InetAddress.getLocalHost().getHostName());
            /**
             * Anonymous Runnable speak, watches the SimpleClient's input stream
             * and sends out to SimpleServer through the socket.
             */
            Runnable speak = () -> {
                try {
                    String fromUser;
                    while (running && (fromUser = stdIn.readLine()) != null) {
                        out.println(fromUser);
                        if (fromUser.equalsIgnoreCase("exit")) {
                            running = false;             
                            throw new IOException("SimpleClient has exit the connection.");
                        }                   
                    }
                } catch (IOException e) {
                    if (socket.isClosed()) {
                        System.out.println("> " + e.getMessage());
                        System.out.println("> Press [return] to confirm exit.");                            
                    } else {
                        return; // So that the message appears only when exit is on standby.     
                    }  
                }
            };
            /**
             * Anonymous Runnable listen, watches this socket's input stream
             * coming from the SimpleServer's output stream, then prints the
             * line to System.out.
             */
            Runnable listen = () -> {
                try {
                    String fromSocket;
                    while (running && (fromSocket = in.readLine()) != null) {                      
                        if (fromSocket.equalsIgnoreCase("exit")) {
                            running = false;
                            throw new IOException("SimpleServer has exit the connection.");
                        }
                        System.out.println(fromSocket);
                    }
                } catch (IOException e) {
                    if (socket.isClosed()) {
                         return; // So that the message appears only when exit is on standby.                       
                    } else {
                        System.out.println("> " + e.getMessage());
                        System.out.println("> Press [return] to confirm exit.");
                    }  
                }
            };
            Thread listenThread = new Thread(listen); 
            Thread speakThread = new Thread(speak);         
            listenThread.start();          
            speakThread.start();                
            while (running) {
                Thread.sleep(200);
                if (!running) {
                    throw new IOException("SimpleClient is no longer running.");
                }
            }
        } catch (InterruptedException e) {
            System.err.println("> InterruptedException: " + e.getMessage());
        } catch (UnknownHostException e) {
            System.err.println("> UnknownHostException: " + e.getMessage());
        } catch (IOException e) {
           System.out.println("> Exiting Program...\n");
        } finally {
             System.exit(0);
        }
    }
}
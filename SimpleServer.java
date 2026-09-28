import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.SocketTimeoutException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
/**
 * 
 * Paper:       COMPX204-24B
 * Assignment:  1 Introduction to Sockets
 * Task:        3a SimpleServer (Simple Network)
 * Name:        Kalei Esteves (u:/ke131)
 * ID:          1282608
 * 
 **/
/**
 * SimpleServer Class.
 * SimpleServer opens a ServerSocket on a port and waits for a SimpleClient to
 * accept. When connected, sends a welcome message, then continues to echo the
 * socket input stream back to the SimpleClient. 
 * 
 * Please note: I've attempted to use threads! I've included commented lines 
 *              to stop the program before this, if necessary.
 * 
 * This class uses private static methods to support custom String formats.
 * @see     SimpleServer#format(String str)
 * @see     SimpleServer#format(String a, String b)
 * */
public class SimpleServer {           

    private static volatile boolean running = true; // Flag used in threads.
    /**
     * Static method main is the entry point to this program.
     * 
     * Note: Args may be left blank to find an available port.                  
     * @param   args    Specifies the user input from the command-line.
     */  
    public static void main(String[] args) {

        int portNumber = 0;
        if (args.length == 1) {
            portNumber = Integer.parseInt(args[0]);
        } else if (args.length > 1) {
            printIndentln("Usage 1: 'java SimpleServer <port number>'");
            printIndentln("Usage 2: 'java SimpleServer'");
            System.exit(0);
        }
        // Tries with specified port or finds available port if portNumber = 0
        try (
            ServerSocket serverSocket = new ServerSocket(portNumber);
        ) {      
            //String server = InetAddress.getLocalHost().getHostAddress(); // Uncomment to use address instead of name                 
            String server = InetAddress.getLocalHost().getHostName();           
            String port = String.valueOf(serverSocket.getLocalPort()); 

            printInstructions(server, port); // Prints the command to copy and paste on client-side
            serverSocket.setSoTimeout(20000); // Waits up to 20 seconds to accept a SimpleClient    

            // Tries with new socket and IO resources to accept and open a connection.
            try (
                Socket socket = serverSocket.accept();
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            ) { 
                // *************************************************************  
                // THE FOLLOWING BLOCK COVERS THE ASSIGNMENT SPECIFICATIONS          
                String remoteName = "SimpleClient"; 
                String remoteAddress = socket.getInetAddress().getHostAddress();
                while (remoteName == "SimpleClient") {
                    if ((remoteName = in.readLine()) != null) {                     
                        out.println(String.format("Hello, %s.", remoteName));
                        out.println(String.format("Your IP address is %s", remoteAddress));
                    }
                }
                //out.println("exit");
                //running = false;
                // UNCOMMENT THE TWO LINES ABOVE TO END THE PROGRAM HERE.
                // *************************************************************   

                String clientName = remoteName;
                String clientAddress = remoteAddress;
                printIndentln("A SimpleClient was accepted!");
                printIndentln("IP Address:", clientAddress);
                printIndentln("Name:", clientName);  
                printIndentln("Enter 'exit' to close the connection.");                 
                /**
                 * Anonymous Runnable manage, watches this SimpleServer's input 
                 * stream and checks if the user wants to exit, then sends the
                 * signal out to SimpleClient if so.
                 */
                Runnable manage = () -> {
                    try {
                        String fromManager;
                        while (running && (fromManager = stdIn.readLine()) != null) {
                            if (fromManager.equalsIgnoreCase("exit")) {
                                running = false;
                                out.println(fromManager); // Tells the client to exit as well.
                                throw new IOException("Connection is closing on server side.");
                            }
                        } 
                    } catch (IOException e) {                        
                        if (socket.isClosed()) {
                            printIndentln("Press [return] to confirm exit.");                  
                        } else {
                            printIndentln(e.getMessage());
                            return; // So that the message appears only when exit is on standby.  
                        }                       
                    }
                };
                /**
                 * Anonymous Runnable echo, watches this socket's input stream
                 * coming from the SimpleClient, then echoes the line back 
                 * through the socket. 
                 */
                Runnable echo = () -> {
                    int i = 0;
                    try {
                        String fromSocket;
                        while (running && (fromSocket = in.readLine()) != null) {
                            printIndentln("Total messages received: ", String.valueOf(++i));

                            out.println(echoln(clientName, server, fromSocket));

                            if (fromSocket.equalsIgnoreCase("exit")) {
                                running = false;
                                out.println(fromSocket); // Just in case...
                                throw new IOException("Connection is closing on client side.");
                            }
                        } 
                    } catch (IOException e) {
                        if (socket.isClosed()) {
                            return; // It took me ages to figure out a clean exit.                   
                        } else {
                            printIndentln(e.getMessage());
                            printIndentln("Press [return] to confirm exit.");
                        } 
                    }
                };
                /*
                This is where the actual execution of threads occur.
                New threads are created with the runnable objects above.
                They're started, and a while loop blocks until 'running'
                is flagged as false.
                */
                Thread manageThread = new Thread(manage);
                Thread echoThread = new Thread(echo);
                manageThread.start();
                echoThread.start();
                while (running) {
                    Thread.sleep(200); // This seemed to work better than 'join'
                    if (!running) {
                        throw new IOException("SimpleServer is no longer running.");
                    }
                }
                /*
                The following catch-blocks throw new IOExceptions to form a chain
                to the outer IOException block, which then calls System.exit(0).
                This ensures that resources are opened closed automatically, 
                within their respective try-with-resources and catch structure.
                */
            } catch (SocketTimeoutException e) {
                throw new IOException(e.getMessage());
            } catch (InterruptedException e) {
                throw new IOException(e.getMessage()); 
            } catch (UnknownHostException e) {
                throw new IOException(e.getMessage()); 
            } catch (IOException e) {
                throw new IOException(e.getMessage()); 
            }
        } catch (IOException e) {
            printIndentln("Exiting Program:", e.getMessage());
            System.exit(0);     
            // System.exit(1); // Not sure whether 0 or 1  
        }
    } 
    // *************************************************************************  
    // END MAIN PROGRAM
    // *************************************************************************  
    // THE FOLLOWING CODE CONTAINS STATIC METHODS FOR HANDLING STRINGS      
    /**
     * The printInstructions method takes the server name or address, the
     * current port, and then prints, to System out, the instructions for 
     * the client to enter on their side. 
     * @param   server  Specifies the String DNS or Address of this SimpleServer.
     * @param   port    Specifies the String port number of this ServerSocket.
     */
    private static void printInstructions(String server, String port) {
        System.out.println("");
        printIndentln("SimpleServer is now listening on port", port);
        printIndentln("To connect, a SimpleClient should enter:");
        System.out.println("");
        System.out.println("java SimpleClient " + server + " " + port);
        System.out.println("");
        printIndentln("If no SimpleClient is accepted after 20 seconds,");
        printIndentln("SimpleServer will close.");
        printIndentln("Waiting...");
    }
    /**
     * Uses a new line and a blank String to indent one String by 10 characters.
     * Prints to System out.
     * @see     Formatter#format(String format, Object... args)
     * @param   a   Specifies the message.
     */
    private static void printIndentln(String str) {
        System.out.println(String.format("%10s %s", " ", str));
    }
    /**
     * Uses a new line and a blank String to indent two Strings by 10 characters.
     * Prints to System out.
     * @see     Formatter#format(String format, Object... args)
     * @param   a   Specifies the first String variable.
     * @param   b   Specifies the second String variable.
     */
    private static void printIndentln(String a, String b) {
         System.out.println(String.format("%10s %s %s", " ", a, b));
    }
    /**
     * This echoln method returns a label and message.
     * @see     Formatter#format(String format, Object... args)
     * @param   client  Specifies the current SimpleClient's label.
     * @param   server  Specifies this local SimpleServer's label.
     * @param   str     Specifies the message to send back.
     * @return  The output line reformatted to send back to SimpleClient.
     */
    private static String echoln(String client, String server, String str) {
         return String.format("%s@%s: %s", client, server, str);
    }
}
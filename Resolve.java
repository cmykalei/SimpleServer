import java.net.InetAddress;
import java.net.UnknownHostException;
/**
 * 
 * Paper:       COMPX204-24B
 * Assignment:  1 Introduction to Sockets
 * Task:        1 Resolve
 * Name:        Kalei Esteves (u:/ke131)
 * ID:          1282608
 * 
 **/
/**
 * Class Resolve.
 * Resolve performs a search for the Domain Names of IPv4 Addresses.
 * 
 * This class has a static main method and takes arguments from the command
 * line. The results of the search are formatted and printed to System.out. 
 * 
 * @see     InetAddress
 */
public class Resolve{
	
	/**
     * Static method main is the entry point to this program.
     * 
     * Takes arguments of domain names from the System's command-line,
     * then performs a search to resolve the IPv4 Addresses of each one.
     *
     * @param  args     The domain names, given by the user's command-line input.
     */
	public static void main(String[] args){

		if(args.length == 0){		
			System.err.println("Error: no domain name was entered.");
			System.out.println("Usage: <domain name 1> <domain name 1> ... <domain name N>");
		} else {
			System.out.println("\nPerforming Reverse DNS Look-up...\n");	
			for(String arg: args){
				String result = String.format("%s : %s ", arg, "<Unresovled>");
				try{
					result = String.format("%s : %s ", arg, InetAddress.getByName(arg).getHostAddress()); 
                    System.out.println(result); 
				}
				catch(UnknownHostException e){
	                System.out.println(result + "\t" + e.getMessage());
				}
			}
			System.out.println("\nIP Address Lookup Complete.\n");
		}
	}
}
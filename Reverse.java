import java.net.InetAddress;
import java.net.UnknownHostException;
import java.lang.SecurityException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
/**
 * 
 * Paper:       COMPX204-24B
 * Assignment:  1 Introduction to Sockets
 * Task:        2 Reverse
 * Name:        Kalei Esteves (u:/ke131)
 * ID:          1282608
 * 
 **/
/**
 * Class Reverse.
 * Reverse performs a reverse DNS search to find the domain name of a host given
 * its IP address.
 * 
 * This class has a static main method and takes arguments from the command
 * line. The results of the search are formatted and printed to System.out. 
 * 
 * @see     InetAddress
 * @see     Pattern
 * @see     Matcher
 */
public class Reverse {
    /**
     * Static method main is the entry point to this program.
     * 
     * Takes arguments of IPv4 Addresses from the System's command-line,
     * then performs a reverse DNS search to resolve the fully qualified
     * domain names of each one.
     *
     * @param  args     The IP addresses, given by the user's command-line input.
     */
    public static void main(String[] args) {
        
        if (args.length == 0) {
            System.out.println("\nUsage: <IP Address 1> <IP Address 2>... <IP Address N>\n");
            return;
        } else {
            System.out.println("\nPerforming Reverse DNS Look-up...\n");
            for (String arg: args) {  
                String result = String.format("%s : %s ", arg, "<Unresolved>"); 
                try {
                    InetAddress address = InetAddress.getByName(arg); 
                    String name = address.getCanonicalHostName();
                    if (name.compareTo(arg) == 0) {
                        throw new SecurityException("A textual reperesentation of the IP address was returned.");
                    }
                    /*
                    The following block creates a Pattern and Matcher to
                    check if the given CanonicalHostName is valid domain
                    name, by checking if it ends in numbers: 
                        Like '[0-9]$'
                        So that 'edge-star-mini-shv-01-akl1.facebook.com'
                        Returns ''

                    Then calls static method getNormalHostName which takes a
                    fully qualified domain name and formats it into a 
                    reader-friendly String. 
                    */
                    Pattern pattern = Pattern.compile("[0-9]$");
                    Matcher matcher = pattern.matcher(name);
                    if (matcher.find()) {
                        throw new UnknownHostException("A strange domain name format was returned.");
                    } else {
                        result = String.format("%s : %s ", arg, getNormalHostName(name)); 
                        System.out.println(result); 
                    }
                } catch (SecurityException | UnknownHostException e) { 
                    System.err.println(result + "\t" + e.getMessage());        
                }
            } 
            System.out.println("\nReverse Lookup Complete.\n");
        }
    } 

    /**
     * The getNormalHostName method takes a FQDN returns the relevant parts.
     * 
     * @param   fqdn    The fully qualified domain name to re-format.
     * @return  The domain name as a String, shortened to the relevant parts.
     */
    private static String getNormalHostName(String fqdn) { 

        // Quick work-around while I was testing:
        /*if (fqdn.contains("1e100.net")) {
            return fqdn + "(Google DNS)";
        }*/
        //  Actual implementation: split by '.' and also check for '-'
        String[] part = fqdn.split("\\.");
        int n = part.length;
        if (n <= 1) {
            return fqdn;
        } else if (n == 2 || part[n - 3].contains("-")) {
            return part[n - 2] + "." + part[n - 1];
        } else {
            return part[n - 3] + "." + part[n - 2] + "." + part[n - 1];             
        }
    }
}
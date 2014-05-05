package fow.dmapp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
/**
 * Class with static utility methods for the app.
 * 
 * IP address validation courtesy of
 * http://www.mkyong.com/regular-expressions/how-to-validate-ip-address-with-regular-expression/
 * 
 * @author Ben
 *
 */
public class Utils{

    private static Pattern pattern;
    private static Matcher matcher;
 
    private static final String IPADDRESS_PATTERN = 
		"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
 
    static {
	  pattern = Pattern.compile(IPADDRESS_PATTERN);
    }
 
   /**
    * Validate ip address with regular expression
    * @param ip ip address for validation
    * @return true valid ip address, false invalid ip address
    */
    public static boolean validateIP(final String ip){		  
	  matcher = pattern.matcher(ip);
	  return matcher.matches();	    	    
    }
}
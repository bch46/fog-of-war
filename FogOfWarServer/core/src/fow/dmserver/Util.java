package fow.dmserver;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class Util {
    /**
     * Attempts to find the current machine's public IP address by iterating through the available
     * network interfaces and looking for one with non-loopback IP. This method tends to fuck up if
     * there are several active network interfaces and not all of them are facing the internet.
     * 
     * @return String containing the IPv4 address.
     * @author Albert
     */
    public static String findIp() {
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface i = ifaces.nextElement();
                Enumeration<InetAddress> iaddrs = i.getInetAddresses();
                while (iaddrs.hasMoreElements()) {
                    InetAddress ina = iaddrs.nextElement();
                    if (!ina.isLoopbackAddress() && !ina.getHostAddress().contains(":"))
                        return ina.getHostAddress();
                }
            }
            return InetAddress.getLocalHost().getHostAddress();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }
}

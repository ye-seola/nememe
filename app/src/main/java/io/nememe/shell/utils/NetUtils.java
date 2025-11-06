package io.nememe.shell.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NetUtils {
    public static void printAddresses()  {
        System.out.println("Addresses");

        List<String> ipv4List = new ArrayList<>();
        List<String> ipv6List = new ArrayList<>();

        try {
            for (var intf : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                var addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (addr.isLoopbackAddress()) continue;

                    String ip = addr.getHostAddress();
                    if (addr instanceof java.net.Inet4Address) {
                        ipv4List.add(ip);
                    } else if (addr instanceof java.net.Inet6Address) {
                        ipv6List.add(ip);
                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        Collections.sort(ipv4List);
        Collections.sort(ipv6List);

        System.out.println("IPv4:");
        for (String addr : ipv4List) {
            System.out.println("- " + addr);
        }

        System.out.println("\nIPv6:");
        for (String addr : ipv6List) {
            System.out.println("- " + addr);
        }

        System.out.println();
    }
}

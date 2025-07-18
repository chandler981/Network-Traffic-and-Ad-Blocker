/*
 * Author:       Chandler Ward
 * Written:      7 / 3 / 2025
 * Last Updated: 7 / 14 / 2025
 * 
 * Class that will decide if a request is to be blocked
 * on the determined domain or IP
 * 
 */

package com.networktracking.networktracking.Proxy;

import java.util.HashSet;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

@Component
public class BlockedRequestEvaluator {
    
    private final RestTemplate blockHosts = new RestTemplate();
    HashSet<String> blockedDomains = new HashSet<String>();
    

    /*
     * uses RestTemplate from SpringBoot to request the file 'hosts' to be able to parse and then use to block requests
     * This method is called on when the program starts from the main method in NetworktrackingApplication class so that
     * the HashSet blockedDomains object can store all of the currently gathered domains that deal with being
     * ads/malware(possibly)/spam etc. -- so incoming requests or packets that pass through the proxy server can be compared the packets
     * to what is gathered and block it if needed
     */
    @PostConstruct
    public void RequestList(){

        String hostFile = blockHosts.getForObject("https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts", String.class);

        // String hostFile = "example.com";
        if(hostFile != null){
            String[] lines = hostFile.split("\n");
            for(String line : lines){
                String[] parts = line.toLowerCase().trim().split("\\s+");

                if(line.startsWith("#")){continue;}

                if(parts.length >= 2){
                    String domain = parts[1];                   
                    blockedDomains.add(domain);
                }
            }
            // System.out.println(blockedDomains); //console print to check everything is being added
            
        }
        else{
            System.out.println("Not working");
        }
        // blockedDomains.add("example.com");
    }

    //This method will simply take in a domain from the ProxyRequestFilter class and then check the blockedDomains
    //HashSet if that domain is in the set, if true block it, if false let it pass
    public boolean isBlocked(String domain){
        return blockedDomains.contains(domain.toLowerCase());
    }
}

/*
 * Author:       Chandler Ward
 * Written:      7 / 3 / 2025
 * Last Updated: 8 / 5 / 2025
 * 
 * Class that will decide if a request is to be blocked
 * on the determined domain or IP
 * 
 */

package com.networktracking.networktracking.Proxy;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.annotation.PostConstruct;

@Component
public class BlockedRequestEvaluator {
    
    private final RestTemplate blockHosts = new RestTemplate();
    HashSet<String> blockedDomains = new HashSet<String>();
    String hostFile;
    

    /*
     * uses RestTemplate from SpringBoot to request the file 'hosts' to be able to parse and then use to block requests
     * This method is called on when the program starts from the main method in NetworktrackingApplication class so that
     * the HashSet blockedDomains object can store all of the currently gathered domains that deal with being
     * ads/malware(possibly)/spam etc. -- so incoming requests or packets that pass through the proxy server can be compared the packets
     * to what is gathered and block it if needed
     */
    @PostConstruct
    public void RequestList(){
        this.hostFile = blockHosts.getForObject("https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts", String.class);

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
        }
        else{
            System.out.println("Not working");
        }
        blockedDomains.add("example.com");
        blockedDomains.add("https://www.food.com/");
    }

    //This method will simply take in a domain from the ProxyRequestFilter class and then check the blockedDomains
    //HashSet if that domain is in the set, if true block it, if false let it pass
    public boolean isBlocked(String domain){
        return blockedDomains.contains(domain.toLowerCase());
    }
        
    public boolean shouldBlock(JsonNode json) {
        return containsAdKeyword(json);
    }

    //this method is used for the shouldBlock() class that takes in the JsonNode object created from the jackson library
    private boolean containsAdKeyword(JsonNode jsonNode) {
        if (jsonNode.isObject() || jsonNode.isContainerNode()) {
            Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey().toLowerCase();
                String value = field.getValue().toString().toLowerCase();

                System.out.println(key + " and " + value);

                if(this.hostFile.contains(key) || this.hostFile.contains(value)){
                    System.out.println("The github string list was used correctly");
                    return true;
                }

                if (field.getValue().isContainerNode()) {
                    if (containsAdKeyword(field.getValue())) {
                        return true;
                    }
                }
            }
        } else if (jsonNode.isTextual()) {
            return jsonNode.asText().toLowerCase().contains("ad");
        }
        return false;
    }

    public boolean shouldBlock(Map<String, String> formData) {
        for(Map.Entry<String, String> entry : formData.entrySet()){
            if(entry.getKey().toLowerCase().contains("ad") || entry.getValue().toLowerCase().contains("ad")){
                return true;
            }
        }
        return false;
    }
}

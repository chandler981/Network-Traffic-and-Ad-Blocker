/*
 * Author:       Chandler Ward
 * Written:      6 / 27 / 2025
 * Last Updated: 7 / 2 / 2025
 * 
 * This method is the Service class that assists the controller class
 * by storing and managing logs
 *  
 * 
 */

package com.networktracking.networktracking.TrafficTrackingServices;

import java.util.ArrayList;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.networktracking.networktracking.Model.TrackingBlocking;

@Service
public class ProxyLogService {

    ArrayList<TrackingBlocking> blockedReqsList = new ArrayList<TrackingBlocking>();

    //used inside HttpsFiltersAdapter portion and stores info that matches a bad domain in a list
    public void createBlockedRequest(String domain, String address, String protocol){
        TrackingBlocking blockedReq = new TrackingBlocking(); //creates new object with no passed data to avoid errors
        
        try{
            blockedReq.setDomain(domain);
            blockedReq.setIPAddress(address);
            blockedReq.setProtocolUsed(protocol);
            blockedReqsList.add(blockedReq);
        }
        catch(Exception e){
            System.out.println("Error occured due to: " + e);
        }
    }
    
    //Returns list so controller can render it with Thymeleaf and used in a @GetMapping("/") method
    public ArrayList<TrackingBlocking> getBlockedRequests(){
        try{
            return blockedReqsList;
        }
        catch(Exception e){
            System.out.println("This error occured when attempting to get all blocked requests: " + e);
            return new ArrayList<TrackingBlocking>();
        }
    }

    //Clears the ArrayList and log so it doesnt get too large over time and slow stuff down or use too much memory
    @Scheduled(fixedRate = 600000)
    public void clear(){
        try{
            if(blockedReqsList.size() > 200){
                blockedReqsList.clear();
            }
        }
        catch(Exception e){
            System.out.println("This error occured when trying to clear log or ArrayList: " + e);
        }

    }
}

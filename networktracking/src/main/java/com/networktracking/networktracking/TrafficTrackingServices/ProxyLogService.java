/*
 * Author:       Chandler Ward
 * Written:      6 / 27 / 2025
 * Last Updated: 6 / 27 / 2025
 * 
 * This method is the Service class that assists the controller class
 * by storing and managing logs
 *  
 * 
 */

package com.networktracking.networktracking.TrafficTrackingServices;

import org.springframework.stereotype.Service;

@Service
public class ProxyLogService {

    //used inside HttpsFiltersAdapter portion and stores a domain that matches a bad domain in a list
    public void createBlockedRequest(){

    }
    
    //Returns list so controller can render it with Thymeleaf and used in a @GetMapping("/") method
    public void getBlockedRequests(){

    }

    //Clears the log so it doesnt get too large over time and slow stuff down or use too much memory
    public void clear(){


    }
}

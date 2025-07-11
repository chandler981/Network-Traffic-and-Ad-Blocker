/*
 * Author:       Chandler Ward
 * Written:      7 / 3 / 2025
 * Last Updated: 7 / 8 / 2025
 * 
 * Subclass of HttpFiltersAdapter that will be used to customize filtering logic
 * for what is to be blocked in terms of domain or IP
 * 
 * Handles filtering logic by overriding methods, decides blocking/allowing packets,
 * logging the blocked data through the ProxyLogService class and then 
 * returning appropritae proxy repsonses when blocking
 * 
 */

package com.networktracking.networktracking.Proxy;

import org.littleshoot.proxy.HttpFiltersAdapter;

import com.networktracking.networktracking.TrafficTrackingServices.ProxyLogService;

import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

public class ProxyRequestFilter extends HttpFiltersAdapter{

    private final ProxyLogService proxyLogService;

    public ProxyRequestFilter(HttpRequest originalRequest, ProxyLogService proxyLogService) {
        super(originalRequest);
        this.proxyLogService = proxyLogService;
    }

    /*
     * Method to handle eval/filtering/blocking packets
     * pass information to parseNeededInfo() to get the domain of the http/https request
     * this will then be used to evaluate if request should be blocked by calling on 
     * BlockedRequestEvaluator method isBlocked() to evaluate if its to be blocked
     * if it is blocked blockResponse() will be called to then get the HTTP response 
     * and handle it effectively
     */
    public void packetHandler(){

    }
    
    /*
     * Method returning blocking response
     * This method will return what is needed to block the packet or request
     * from actually going through
     */
    public HttpResponse blockResponse(){

        return null; //this is place holder until method is finished to stop errors
    }

    /*
     * Method for logging request if it was blocked, so send it to proxyLogService to create a new object
     * Will send new blocked request domain + IP + protocol to proxyLogService createBlockedRequest() to create
     * a new object to be added to the users view
     */
    public void logNewBlockRequest(){

    }

    //Method added in to test some stuff and look at packets of data that are coming in
    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
        System.out.println("Received Request: " + originalRequest + "\n");
        System.out.println(httpObject);
        return null; // return null means continue the request as normal
    }

    //method to parse the domain from the httpObject that is obtained from the proxy
    public String parseNeededInfo(){

        
        return "";
    }

}

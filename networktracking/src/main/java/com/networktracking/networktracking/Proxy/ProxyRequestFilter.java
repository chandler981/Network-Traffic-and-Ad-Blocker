/*
 * Author:       Chandler Ward
 * Written:      7 / 3 / 2025
 * Last Updated: 7 / 14 / 2025
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

import java.nio.charset.StandardCharsets;

import org.littleshoot.proxy.HttpFiltersAdapter;

import com.networktracking.networktracking.TrafficTrackingServices.ProxyLogService;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class ProxyRequestFilter extends HttpFiltersAdapter{

    private final ProxyLogService proxyLogService;
    private final BlockedRequestEvaluator blockEval;

    public ProxyRequestFilter(HttpRequest originalRequest, ProxyLogService proxyLogService, BlockedRequestEvaluator blockedEvaluator) {
        super(originalRequest);
        this.proxyLogService = proxyLogService;
        this.blockEval = blockedEvaluator;

    }

    //This override method will be used to filter requests on their way from the client to proxy server
    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
        
        System.out.println(httpObject + " http object print out \n");

        if (httpObject instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) httpObject;
            System.out.println("Host: " + httpRequest.headers().get("Host"));
            System.out.println("Full URI: " + httpRequest.uri());
            System.out.println("Headers: " + httpRequest.headers());

            if(packetHandler(httpRequest)){
                return blockResponse();
            }
        }
        return null; // return null means continue the request as normal
    }

    /*
     * Method to handle eval/filtering/blocking packets
     * pass information to parseNeededInfo() to get the domain of the http/https request,
     * this will then be used to evaluate if request should be blocked by calling on 
     * BlockedRequestEvaluator method isBlocked() to evaluate if its to be blocked,
     * if it is blocked, blockResponse() will be called to then get the HTTP response 
     * and handle it effectively
     */
    public Boolean packetHandler(HttpRequest httpRequest){
        // System.out.println(httpRequest);
        String domain = parseNeededInfo(httpRequest); //passes the httpRequest to parseNeededInfo(), this request was passed to packetHandler() from clientToProxyRequest()
        if(this.blockEval.isBlocked(domain)){
            logNewBlockRequest(domain, "Not being Used", "Not being Used");
            return true;
        }
        return false;

    }   
    
    /*
     * Method returning blocking response
     * This method will return what is needed to block the packet or request
     * from actually going through
     */
    public HttpResponse blockResponse(){
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1, 
            HttpResponseStatus.FORBIDDEN,
            Unpooled.copiedBuffer("Access Denied: This request has been blocked", StandardCharsets.UTF_8)
        );
        
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        
        return response;
    }

    /*
     * Method for logging request if it was blocked, so send it to proxyLogService to create a new object
     * Will send new blocked request domain + IP + protocol to proxyLogService createBlockedRequest() to create
     * a new object to be added to the users view
     */
    public void logNewBlockRequest(String domain, String IP, String protocol){
        //currently not getting an actual address or protocol as I havent implemented getting the actual datagram to get that fully
        this.proxyLogService.createBlockedRequest(domain, IP, protocol);
    }

    //method to parse the domain from the httpObject that is obtained from the proxy
    public String parseNeededInfo(HttpRequest httpRequest){
        String domain = httpRequest.headers().get("Host");
        System.out.println(domain);
        
        return domain;
    }

}

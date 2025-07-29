/*
 * Author:       Chandler Ward
 * Written:      7 / 3 / 2025
 * Last Updated: 7 / 29 / 2025
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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.littleshoot.proxy.HttpFiltersAdapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networktracking.networktracking.TrafficTrackingServices.ProxyLogService;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;

public class ProxyRequestFilter extends HttpFiltersAdapter{

    private final ProxyLogService proxyLogService; //creates object to be used to access methods from ProxyLogService but doesnt assign it yet
    private final BlockedRequestEvaluator blockEval; //creates object to be used to access methods from BlockedRequestEvaluator but doesnt assign it yet
    private ByteBuf bodyBuffer = Unpooled.buffer(); //creates object to be used from the Java library Netty.buffer, ByteBuf, to store data 
                                                    //from the HTTP(S) requests to use once the last POST request is made

    public ProxyRequestFilter(HttpRequest originalRequest, ProxyLogService proxyLogService, BlockedRequestEvaluator blockedEvaluator) {
        super(originalRequest);
        this.proxyLogService = proxyLogService;
        this.blockEval = blockedEvaluator;
    }

    //This override method will be used to filter requests on their way from the client to proxy server
    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
        
        // System.out.println(httpObject + " http object print out \n");

        // System.out.println(this.originalRequest.uri());

        if (httpObject instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) httpObject;
            
            // This if ... else block serves no purpose in terms of logic, its here just to see what is intercepted in the console
            // if (httpRequest.method().name().equalsIgnoreCase("CONNECT")) {
            //     System.out.println("==> HTTPS CONNECT tunnel: " + httpRequest.uri());
            // } else {
            //     System.out.println("==> Intercepted HTTP(S) request: " + httpRequest.method() + " " + httpRequest.uri());
            //     System.out.println("   Host: " + httpRequest.headers().get("Host"));
            // }

            if(packetHandler(httpRequest)){
                return blockResponse();
            }
        }
        else if(httpObject instanceof HttpContent){
            HttpContent httpContent = (HttpContent) httpObject;

            bodyBuffer.writeBytes(httpContent.content());

            try{
                if(httpContent instanceof LastHttpContent){ //This is entered if the last POST portion of the request is collected to complete the total body of the HTTP(S) request
                    String contentType = this.originalRequest.headers().get("Content-Type"); //holds the entire body to be parsed 
                    
                    if(contentType != null){
                        //here the bodyBuffer is converted to a string and parsed as a JSON
                        // System.out.println("This is the domain: " + httpContent.toString());

                        if(contentType.contains("application/json")){
                            String convertedData = bodyBuffer.toString(StandardCharsets.UTF_8); //StandardCharsets.UTF_8 is used get the correct results so nothing is still un readable
                            System.out.println(convertedData);
                            JsonNode json = parseJson(convertedData); //creates a JsonNode object that will have a returned object from the parseJson() method
                            
                            if(json == null){
                                return null;
                            }
                            if(this.blockEval.shouldBlock(json)){
                                blockResponse();
                            }
    
                            //if json data contains anything dealing with an ad, block it or modify a response
                        }
                        //here its just decoded form fields
                        else if(contentType.contains("application/x-www-form-urlencoded")){
                            String formData = bodyBuffer.toString(StandardCharsets.UTF_8);
                            Map<String, String> parameters = parseFormData(formData);
    
                            if(this.blockEval.shouldBlock(parameters)){
                                blockResponse();
                            }
                        }
                    }
                    else{
                        System.out.println("No content-type header in request: " + this.originalRequest.uri());
                    }
                    //Inspect data and handle accordingly depending on what it is
                }   
            }
            //always happens after the try{} block is completed to avoid any possibly early if() statement exits
            //to avoid any memory bloat etc.
            finally{
                /* the .release() method isnt used since the buffer is just being emptied for re use and 
                 * using .release() would need to re create the object which would slow things down instead of just clearing the buffer.
                 * However if there seems to be some large memory issue in the future implement this and deal witht he slow down with Unpooled.buffer()
                 * like when the global variable is created at the top of the class.
                 */
                bodyBuffer.clear(); //clears the buffer for re use
            }
        }
        return null; // return null means continue the request as normal
    }

    //parses data if its not in JSON form and then puts it to a Map object to be used later on and then returns it back to the clientProxyRequest() method
    private Map<String, String> parseFormData(String formData) {
        Map<String, String> returnedMap = Arrays.stream(formData.split("&"))
                                                .map(s -> s.split("=", 2))
                                                .filter(pair -> pair.length == 2)
                                                .collect(Collectors.toMap(pair -> URLDecoder.decode(pair[0], StandardCharsets.UTF_8), 
                                                                        pair -> URLDecoder.decode(pair[1], StandardCharsets.UTF_8)
                                                ));
        // System.out.println("Parsed Form Data: " + returnedMap);

        return returnedMap;
    }

    
    //parses data if its in JSON and then puts it to a JsonNode object to be used later on and then returns it back to the clientProxyRequest() method
    private JsonNode parseJson(String convertedData){
        ObjectMapper mapper = new ObjectMapper();
        try{
            return mapper.readTree(convertedData);
        }
        catch(JsonProcessingException e){
            e.printStackTrace();
            return null;
        }
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
        System.out.println("This is the domain name for HTTP: " + domain);
        
        return domain;
    }

}

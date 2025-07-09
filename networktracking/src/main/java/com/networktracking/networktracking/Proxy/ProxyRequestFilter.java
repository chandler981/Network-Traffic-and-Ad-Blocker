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

import io.netty.handler.codec.http.HttpRequest;

public class ProxyRequestFilter extends HttpFiltersAdapter{

    private final ProxyLogService proxyLogService;

    public ProxyRequestFilter(HttpRequest originalRequest, ProxyLogService proxyLogService) {
        super(originalRequest);
        this.proxyLogService = proxyLogService;
    }

    // Method to handle eval/filtering/blocking packets

    // Method returning blocking response

    // Method for logging request if it was blocked, so send it to proxyLogService to create a new object

    
}

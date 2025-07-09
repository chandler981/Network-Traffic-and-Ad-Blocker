/*
 * Author:       Chandler Ward
 * Written:      7 / 8 / 2025
 * Last Updated: 7 / 8 / 2025
 * 
 * This class handles running the proxy server when the actual program
 * itself is started as its annotated to be a @Component
 * 
 */

package com.networktracking.networktracking.Proxy;


import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.springframework.stereotype.Component;

import com.networktracking.networktracking.TrafficTrackingServices.ProxyLogService;

import jakarta.annotation.PostConstruct;

@Component
public class ProxyServerRunner {

    private final ProxyLogService proxyLogService;
    private final ProxyFilterFactory proxyFilterFactory;

    public ProxyServerRunner(ProxyLogService proxyLogService, ProxyFilterFactory proxyFilterFactory){
        this.proxyLogService = proxyLogService;
        this.proxyFilterFactory = proxyFilterFactory;
    }
    
    @PostConstruct
    public void startProxyServer(){
        HttpProxyServer server =
            DefaultHttpProxyServer.bootstrap()
                .withPort(3128)
                .withFiltersSource(proxyFilterFactory)
                .start();
    }
    
}

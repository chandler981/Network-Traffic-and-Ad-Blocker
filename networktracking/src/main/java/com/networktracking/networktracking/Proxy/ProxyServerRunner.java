/*
 * Author:       Chandler Ward
 * Written:      7 / 8 / 2025
 * Last Updated: 7 / 9 / 2025
 * 
 * This class handles running the proxy server when the actual program
 * itself is started as its annotated to be a @Component
 * 
 */

package com.networktracking.networktracking.Proxy;

import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.springframework.stereotype.Component;

import com.networktracking.networktracking.CertificateClasses.CustomCertMitmManager;
import com.networktracking.networktracking.CertificateClasses.CustomCertMitmManagerFactory;
import com.networktracking.networktracking.TrafficTrackingServices.ProxyLogService;

import jakarta.annotation.PostConstruct;

@Component
public class ProxyServerRunner {
    private final ProxyLogService proxyLogService;
    private final ProxyFilterFactory proxyFilterFactory;
    private final ProxyKeystoreProperties keystoreProperties;

    public ProxyServerRunner(ProxyLogService proxyLogService, ProxyFilterFactory proxyFilterFactory, ProxyKeystoreProperties keystoreProperties){
        this.proxyLogService = proxyLogService;
        this.proxyFilterFactory = proxyFilterFactory;
        this.keystoreProperties = keystoreProperties;
    }

    @PostConstruct
    public void startProxyServer() {
        CustomCertMitmManager mitmManager = CustomCertMitmManagerFactory.create(
            keystoreProperties.getPath(),
            keystoreProperties.getPassword()
        );

        HttpProxyServer server = DefaultHttpProxyServer.bootstrap()
            .withPort(3128)
            .withFiltersSource(proxyFilterFactory)
            .withManInTheMiddle(mitmManager)
            .start();
    }
}
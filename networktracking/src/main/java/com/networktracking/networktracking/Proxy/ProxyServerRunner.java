/*
 * Author:       Chandler Ward
 * Written:      7 / 8 / 2025
 * Last Updated: 7 / 16 / 2025
 *
 * This class handles starting the proxy server when the application launches.
 * It relies on CustomCertMitmManagerFactory to handle SSL certificates using your PKCS12 keystore.
 */

package com.networktracking.networktracking.Proxy;

import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import org.littleshoot.proxy.extras.SelfSignedMitmManager;
import org.springframework.stereotype.Component;

import com.networktracking.networktracking.TrafficTrackingServices.ProxyLogService;

import jakarta.annotation.PostConstruct;

@Component
public class ProxyServerRunner {

    private final ProxyLogService proxyLogService;
    private final ProxyFilterFactory proxyFilterFactory;
    private final ProxyKeystoreProperties keystoreProperties;

    public ProxyServerRunner(ProxyLogService proxyLogService, ProxyFilterFactory proxyFilterFactory, ProxyKeystoreProperties keystoreProperties) {
        this.proxyLogService = proxyLogService;
        this.proxyFilterFactory = proxyFilterFactory;
        this.keystoreProperties = keystoreProperties;
    }

    @PostConstruct
    public void startProxyServer() {
        try {

            HttpProxyServer server = DefaultHttpProxyServer.bootstrap()
                .withPort(9090)
                .withFiltersSource(proxyFilterFactory)
                .withManInTheMiddle(new SelfSignedMitmManager())
                .start();

            System.out.println("Proxy server started successfully on port 9090.");
            System.out.println("Ensure your browser is configured to use localhost:9090 as the proxy.");
            System.out.println("For HTTPS interception, ensure your Root CA certificate is imported into your browser's trust store.");

        } catch (Exception e) {
            System.err.println("Failed to start proxy server with MITM manager.");
            e.printStackTrace();
        }
    }
}

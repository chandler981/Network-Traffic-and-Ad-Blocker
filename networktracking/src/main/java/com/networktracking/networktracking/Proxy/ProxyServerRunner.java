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
import org.littleshoot.proxy.MitmManager;
import org.springframework.stereotype.Component;

import com.networktracking.networktracking.CertificateClasses.CustomCertMitmManagerFactory;
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
            //Use CustomCertMitmManagerFactory to build the MITM manager from keystore
            MitmManager mitmManager = CustomCertMitmManagerFactory.create(
                keystoreProperties.getPath(),
                keystoreProperties.getPassword()
            );

            HttpProxyServer server = DefaultHttpProxyServer.bootstrap()
                .withPort(3128)
                .withFiltersSource(proxyFilterFactory)
                .withManInTheMiddle(mitmManager)
                .start();

            System.out.println("Proxy server started successfully on port 3128.");
            System.out.println("Ensure your browser is configured to use localhost:3128 as the proxy.");
            System.out.println("For HTTPS interception, ensure your Root CA certificate is imported into your browser's trust store.");

        } catch (Exception e) {
            System.err.println("Failed to start proxy server with MITM manager.");
            e.printStackTrace();
        }
    }
}

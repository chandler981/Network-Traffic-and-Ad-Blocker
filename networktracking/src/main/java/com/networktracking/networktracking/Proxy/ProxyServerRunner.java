/*
 * Author:       Chandler Ward
 * Written:      7 / 8 / 2025
 * Last Updated: 7 / 28 / 2025
 *
 * This class handles starting the proxy server when the application launches.
 * It relies on CustomCertMitmManagerFactory to handle SSL certificates using your PKCS12 keystore.
 */

package com.networktracking.networktracking.Proxy;

import java.io.File;

import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import net.lightbody.bmp.mitm.KeyStoreFileCertificateSource;
import net.lightbody.bmp.mitm.manager.ImpersonatingMitmManager;

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

            //creates dynamic CA root certificate generator using default settings (2048-bit RSA keys)
            // RootCertificateGenerator rootCertificateGenerator = RootCertificateGenerator.builder().build();

            //Saves the dynamically generated .cer file for installation to the browser
            // rootCertificateGenerator.saveRootCertificateAsPemFile(new File("my-dynamic-ca.cer"));


            KeyStoreFileCertificateSource fileCertificateSource = 
                                            new KeyStoreFileCertificateSource(
                                                "PKCS12",
                                                new File(keystoreProperties.getPath()),
                                                keystoreProperties.getCaAlias(),
                                                keystoreProperties.getPassword());

            //Tells the mitmManager class to use the new root certificate in its build process
            ImpersonatingMitmManager mitmManager = ImpersonatingMitmManager.builder()
                                                                            .rootCertificateSource(fileCertificateSource)
                                                                            .build();

            HttpProxyServer server = DefaultHttpProxyServer.bootstrap()
                .withPort(9090) //can be changed but this was used as recemendation from the Littleproxy GitHub page
                .withFiltersSource(proxyFilterFactory) //uses the ProxyFilterFactory class as a way to deal with HTTP requests
                .withManInTheMiddle(mitmManager) //lets the Proxy use the newly created mitmManager object
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

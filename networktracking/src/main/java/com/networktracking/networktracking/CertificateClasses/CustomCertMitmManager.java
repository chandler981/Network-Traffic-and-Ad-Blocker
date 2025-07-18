/*
 * Author:       Chandler Ward
 * Written:      7 / 9 / 2025
 * Last Updated: 7 / 14 / 2025
 * 
 * This is the class that handles returning the required 
 * objects and information for the SSL certificates and information
 * 
 */

package com.networktracking.networktracking.CertificateClasses;

import io.netty.handler.codec.http.HttpRequest;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;

import org.littleshoot.proxy.MitmManager;

public class CustomCertMitmManager implements MitmManager{
    private final SSLContext sslContext;

    public CustomCertMitmManager(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    @Override
    public SSLEngine serverSslEngine(String peerHost, int peerPort) {
        return sslContext.createSSLEngine(peerHost, peerPort);
    }

    @Override
    public SSLEngine clientSslEngineFor(HttpRequest httpRequest, SSLSession serverSslSession) {
        SSLEngine sslEngine = sslContext.createSSLEngine();
        sslEngine.setUseClientMode(true);
        return sslEngine;
    }
        
    @Override
    public SSLEngine serverSslEngine() {
        return sslContext.createSSLEngine();
    }

}
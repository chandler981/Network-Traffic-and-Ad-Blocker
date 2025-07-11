/*
 * Author:       Chandler Ward
 * Written:      7 / 9 / 2025
 * Last Updated: 7 / 9 / 2025
 * 
 * This class CustomCertMitmManagerFactory handles passing the needed
 * information to the needed places so that keystore file can be accessed
 * for its password so that the certificates that are needed can be used
 * 
 */

package com.networktracking.networktracking.CertificateClasses;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.*;
import java.security.cert.CertificateException;

public class CustomCertMitmManagerFactory {

    public static CustomCertMitmManager create(String keyStorePath, String keyStorePassword) {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");

            try (FileInputStream keyStoreFile = new FileInputStream(keyStorePath)) {
                keyStore.load(keyStoreFile, keyStorePassword.toCharArray());
            }

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, keyStorePassword.toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

            return new CustomCertMitmManager(sslContext);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException |
                 UnrecoverableKeyException | KeyManagementException | java.io.IOException e) {
            throw new RuntimeException("Failed to initialize custom MITM manager", e);
        }
    }
}
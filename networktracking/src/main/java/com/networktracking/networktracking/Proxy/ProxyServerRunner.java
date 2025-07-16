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
    import org.littleshoot.proxy.MitmManager; // LittleProxy's MitmManager interface
    import org.littleshoot.proxy.mitm.AuthorityMitmManager;
    import org.springframework.stereotype.Component;

    import com.networktracking.networktracking.TrafficTrackingServices.ProxyLogService;
    // Removed imports for CustomCertMitmManager and CustomCertMitmManagerFactory as they are no longer needed
    // import com.networktracking.networktracking.CertificateClasses.CustomCertMitmManager;
    // import com.networktracking.networktracking.CertificateClasses.CustomCertMitmManagerFactory;

    import jakarta.annotation.PostConstruct;

    import java.io.FileInputStream;
    import java.security.KeyStore;
    import java.security.KeyStoreException;
    import java.security.PrivateKey;
    import java.security.cert.X509Certificate;
    import java.util.Enumeration; // For listing keystore aliases (helpful for debugging)

    @Component
    public class ProxyServerRunner {
        private final ProxyLogService proxyLogService;
        private final ProxyFilterFactory proxyFilterFactory;
        private final ProxyKeystoreProperties keystoreProperties; // Assumed to hold path, password, and CA alias

        public ProxyServerRunner(ProxyLogService proxyLogService, ProxyFilterFactory proxyFilterFactory, ProxyKeystoreProperties keystoreProperties){
            this.proxyLogService = proxyLogService;
            this.proxyFilterFactory = proxyFilterFactory;
            this.keystoreProperties = keystoreProperties;
        }

        /*
         * This method is called when the Spring application starts.
         * It initializes and starts the LittleProxy server, configuring it
         * with the ad-blocking filters and the Man-in-the-Middle (MITM)
         * functionality for HTTPS traffic.
         */
        @PostConstruct
        public void startProxyServer() {
            PrivateKey caPrivateKey = null;
            X509Certificate caCert = null;

            try {
                // Load the PKCS12 keystore containing your Root CA's private key and certificate
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                try (FileInputStream keyStoreFile = new FileInputStream(keystoreProperties.getPath())) {
                    keyStore.load(keyStoreFile, keystoreProperties.getPassword().toCharArray());
                }

                // --- OPTIONAL: List aliases to help identify your CA alias if unsure ---
                System.out.println("--- Keystore Aliases (for debugging CA alias) ---");
                Enumeration<String> aliases = keyStore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = aliases.nextElement();
                    System.out.println("- " + alias + (keyStore.isKeyEntry(alias) ? " (Key Entry)" : "") + (keyStore.isCertificateEntry(alias) ? " (Cert Entry)" : ""));
                }
                System.out.println("-------------------------------------------------");
                // --- END OPTIONAL ---

                // Retrieve the Root CA's private key and certificate using the provided alias
                // The 'caAlias' property should be configured in your application.properties/yml
                // or wherever ProxyKeystoreProperties gets its values.
                String caAlias = keystoreProperties.getCaAlias(); // Assuming you add a getCaAlias() method
                if (caAlias == null || caAlias.isEmpty()) {
                    throw new IllegalArgumentException("CA Alias must be provided in ProxyKeystoreProperties.");
                }

                if (!keyStore.containsAlias(caAlias)) {
                    throw new KeyStoreException("CA alias '" + caAlias + "' not found in keystore. " +
                                                "Please verify the alias using `keytool -list -v`.");
                }

                caPrivateKey = (PrivateKey) keyStore.getKey(caAlias, keystoreProperties.getPassword().toCharArray());
                if (caPrivateKey == null) {
                    throw new Exception("Could not retrieve CA private key for alias: " + caAlias + ". Check password or alias type.");
                }

                caCert = (X509Certificate) keyStore.getCertificate(caAlias);
                if (caCert == null) {
                    throw new Exception("Could not retrieve CA certificate for alias: " + caAlias + ". Check alias type.");
                }

            } catch (Exception e) {
                System.err.println("FATAL ERROR: Failed to load CA certificate and key from keystore. HTTPS interception will not work.");
                e.printStackTrace();
                // Depending on your application's needs, you might want to exit here or run without HTTPS interception.
                // For now, we'll just log and continue, but HTTPS will fail.
                return;
            }

            // Initialize the MitmManager from the 'littleproxy-mitm' library
            // This manager will handle dynamic certificate generation and signing
            MitmManager mitmManager = new AuthorityMitmManager(caCert, caPrivateKey);


            // Start the LittleProxy server
            HttpProxyServer server = DefaultHttpProxyServer.bootstrap()
                .withPort(3128) // Your desired proxy port
                .withFiltersSource(proxyFilterFactory) // Your ad-blocking filter
                .withManInTheMiddle(mitmManager)       // The crucial MITM manager for HTTPS
                .start();

            System.out.println("Proxy server started successfully on port 3128.");
            System.out.println("Ensure your browser is configured to use this proxy (e.g., localhost:3128).");
            System.out.println("For HTTPS, you MUST import your Root CA certificate into your browser's trust store.");
        }
    }
package com.networktracking.networktracking.Proxy;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "proxy.keystore")
@Data
public class ProxyKeystoreProperties {
    private String path;
    private String password;
    private String caAlias;

}

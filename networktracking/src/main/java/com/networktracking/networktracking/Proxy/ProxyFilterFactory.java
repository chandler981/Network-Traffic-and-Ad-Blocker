/*
 * Author:       Chandler Ward
 * Written:      6 / 27 / 2025
 * Last Updated: 7 / 14 / 2025
 * 
 * Class that will return HttpFilters or wrap that can then be used for 
 * Spring Injection
 * 
 */

package com.networktracking.networktracking.Proxy;

import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.springframework.stereotype.Component;

import com.networktracking.networktracking.TrafficTrackingServices.ProxyLogService;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

@Component
public class ProxyFilterFactory extends HttpFiltersSourceAdapter{
    private final ProxyLogService proxyLogService;
    private final BlockedRequestEvaluator blockedRequestEvaluator;

    public ProxyFilterFactory(ProxyLogService proxyLogService, BlockedRequestEvaluator blockedRequestEvaluator) {
        this.proxyLogService = proxyLogService;
        this.blockedRequestEvaluator = blockedRequestEvaluator;
    }

    @Override
    public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
        return new ProxyRequestFilter(originalRequest, this.proxyLogService, this.blockedRequestEvaluator);
    }
}

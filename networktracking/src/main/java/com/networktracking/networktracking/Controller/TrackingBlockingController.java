/*
 * Author:       Chandler Ward
 * Written:      6 / 27 / 2025
 * Last Updated: 7 / 2 / 2025
 * 
 * 
 * This is the controller class that is handling the GET and POST methods
 * to update the users view and also clear the logs so that it doesnt become
 * too large and bloated after long use
 * 
 */

package com.networktracking.networktracking.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.networktracking.networktracking.Model.TrackingBlocking;
import com.networktracking.networktracking.TrafficTrackingServices.ProxyLogService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import java.util.List;

@Controller
@RequestMapping("/")
public class TrackingBlockingController {

    private final ProxyLogService proxLogServ;

    public TrackingBlockingController(ProxyLogService proxLogServ){
        this.proxLogServ = proxLogServ;
    }

    // GET /blocked
    @GetMapping("/")
    public String addBlockedRequests(Model model){
        List<TrackingBlocking> sites = proxLogServ.getBlockedRequests();
        model.addAttribute("sites", sites);
        return "layout";
    }
}

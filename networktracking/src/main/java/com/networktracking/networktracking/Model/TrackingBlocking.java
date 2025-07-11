/*
 * Author:       Chandler Ward
 * Written:      6 / 27 / 2025
 * Last Updated: 6 / 27 / 2025
 * 
 * This class is the Model for the view to use when it is adding
 * in the Domain, IP Address, and Protocol of what is being used
 * by whatever is being blocked from being shown
 * 
 */

package com.networktracking.networktracking.Model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

//Model class for creating objects that will be used for displaying to the users view
//in the browser
@Data
@Getter
@Setter
public class TrackingBlocking {
    private String Domain;
    private String IPAddress;
    private String ProtocolUsed;
}

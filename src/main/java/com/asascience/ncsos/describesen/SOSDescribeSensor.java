/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.describesen;

import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author SCowan
 */
public class SOSDescribeSensor extends SOSDescribeStation implements SOSDescribeIF {
    
    public SOSDescribeSensor( NetcdfDataset dataset, String procedure ) {
        super(dataset, procedure);
    }
    
}

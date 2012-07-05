/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.describesen;

import com.asascience.ncsos.outputformatter.DescribeSensorFormatter;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author SCowan
 */
public class SOSDescribeTrajectory extends SOSDescribeStation implements SOSDescribeIF {
    
    public SOSDescribeTrajectory( NetcdfDataset dataset, String procedure ) {
        super(dataset, procedure);
    }

    /*********************
     * Interface Methods *
     *********************/
    
    @Override
    public void SetupOutputDocument(DescribeSensorFormatter output) {
        // system node
        output.setSystemId("station-" + stationName);
        // set description
        formatSetDescription(output);
        // identification node
        formatSetIdentification(output);
        // classification node
        formatSetClassification(output);
        // contact node
        formatSetContactNodes(output);
        // document node
        formatSetDocumentNodes(output);
        // history node
        formatSetHistoryNodes(output);
        // position node
        formatSetPositionNode(output);
        // timeposition node
        formatSetTimePositionNode(output);
        // remove unwanted nodes
        RemoveUnusedNodes(output);
    }
    
    private void RemoveUnusedNodes(DescribeSensorFormatter output) {
        output.deleteLocationNode();
    }

    private void formatSetPositionNode(DescribeSensorFormatter output) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void formatSetTimePositionNode(DescribeSensorFormatter output) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
}

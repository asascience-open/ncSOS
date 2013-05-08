/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.controller;

import com.asascience.ncsos.service.SOSParser;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * @author Abird
 * @version 
 *
 * Test Main Class for assessing speed of enhanceGETRequest
 *
 */
public class testMainClass {

    private static String imeds13 = "tests/main/resources/datasets/sura/watlev_IKE.nc";

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        Writer write = null;
        SOSParser md = null;
        System.out.println("START");
        boolean stop = true;
        NetcdfDataset dataset = null;
        while (stop == true) {
            try {
                dataset = NetcdfDataset.openDataset(imeds13);
                write = new CharArrayWriter();
                md = new SOSParser();
                md.enhanceGETRequest(dataset, "request=GetCapabilities&version=1&service=sos", imeds13);
                write.flush();
                write.close();

                write = null;
                md = null;
                
            } catch (Exception e) {
                stop = false;
                System.out.println("STOP");
            }finally{
                System.gc();
            }
        }
          dataset.close();
    }
}

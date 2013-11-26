/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.error;

import com.asascience.ncsos.outputformatter.ErrorFormatter;
import com.asascience.ncsos.service.BaseRequestHandler;

import java.io.IOException;

public class ExceptionResponseHandler extends BaseRequestHandler {

    public ExceptionResponseHandler() throws IOException {
        super(null);
        formatter = new ErrorFormatter();
    }

    public void setException(String message) {
        ((ErrorFormatter)formatter).setException(message);
    }

    public void setException(String message, String exceptionCode) {
        ((ErrorFormatter)formatter).setException(message, exceptionCode);
    }

    public void setException(String message, String exceptionCode, String locator) {
        ((ErrorFormatter)formatter).setException(message, exceptionCode, locator);
    }

}

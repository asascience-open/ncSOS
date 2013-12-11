package com.asascience.ncsos.outputformatter;

import org.jdom.Element;
import org.jdom.Namespace;

public class ErrorFormatter extends BaseOutputFormatter {

    private final static String TEMPLATE = "templates/exception.xml";

    public ErrorFormatter() {
        super();
    }

    @Override
    protected String getTemplateLocation() {
        return TEMPLATE;
    }
    
    public void setException(String exceptionMessage) {
        Element root = this.getRoot();
        Namespace ns = this.getNamespace("ows");
        root.getChild("Exception", ns).setAttribute("exceptionCode", "NoApplicableCode");
        root.getChild("Exception", ns).getChild("ExceptionText", ns).setText(exceptionMessage);
    }

    public void setException(String exceptionMessage, String code) {
        Element root = this.getRoot();
        Namespace ns = this.getNamespace("ows");
        root.getChild("Exception", ns).setAttribute("exceptionCode", code);
        root.getChild("Exception", ns).getChild("ExceptionText", ns).setText(exceptionMessage);
    }

    public void setException(String exceptionMessage, String code, String locator) {
        Element root = this.getRoot();
        Namespace ns = this.getNamespace("ows");
        root.getChild("Exception", ns).setAttribute("exceptionCode", code).setAttribute("locator", locator);
        root.getChild("Exception", ns).getChild("ExceptionText", ns).setText(exceptionMessage);
    }
}

package thredds.server.metadata.describeSen;


import thredds.server.metadata.util.XMLDomUtils;
import org.w3c.dom.Document;
import thredds.server.metadata.service.DatasetMetaData;
import ucar.ma2.Array;
import ucar.ma2.Index;


/**
 * describe sensor class: parses the get caps
 * @author abird
 */
public class MockDescribeSensorParser {

    private final DatasetMetaData dst;
    private String templateFileLocation = "C:/Documents and Settings/abird/My Documents/NetBeansProjects/ISOTHREDDS/trunk/src/main/java/threads/server/metadata/templates/sosDescribeSensor.xml";
    Document doc;
    private String routeElement;
    private String identifier = "StationId";
 
     MockDescribeSensorParser() {
        this.dst = new DatasetMetaData();
        dst.setTitle("Title");
        dst.setHistory("History");
        dst.setInstitution("Institution");
        dst.setSource("Source");
        dst.setInstitution("ASA");
        dst.setLocation("Location");
    }

     MockDescribeSensorParser(DatasetMetaData dst) {
        this.dst = dst;
    }

    public void parseTemplateXML() {
        doc = XMLDomUtils.getTemplateDom(templateFileLocation);
        setRouteElement(doc.getDocumentElement().getNodeName());
    }

    public Document getDom() {
        return doc;
    }

    private void setRouteElement(String routeElement) {
        this.routeElement = routeElement;
    }

    public String getSystemGMLID() {
        String container = "System";
        String attribute = "gml:id";
        return  XMLDomUtils.getAttributeFromNode(doc,"member",container,attribute);
    }

    public void setSystemGMLID() {
        String container = "System";
        String attribute = "gml:id";

        long length = dst.getStringStationNames().length;

        if (length ==1)
        {
        String stationName = dst.getStringStationNames()[0];
        XMLDomUtils.setAttributeFromNode(doc,"member",container,attribute,dst.getStationPrefix()+(stationName));
        }
        else
        {
        XMLDomUtils.setAttributeFromNode(doc,"member",container,attribute,null);
        }
       
    }

    public void setOrganizationName() {
        String container = "contact";
        String node = "organizationName";
        XMLDomUtils.setNodeValue(doc,container,node,dst.getInstitution());
    }

    public String getOrganizationName() {
       String container = "contact";
       String node = "organizationName";
       return  XMLDomUtils.getNodeValue(doc,container,node);
    }

    public void setSystemGMLID(String value) {
        String container = "System";
        String attribute = "gml:id";
        XMLDomUtils.setAttributeFromNode(doc,"member",container,attribute,value);
    }

    public void setIdentifierName() {
        String container = "identifier";
        String attribute = "name";
        XMLDomUtils.setAttributeFromNode(doc,"IdentifierList",container,attribute,identifier);
    }

   public String getIdentifierName() {
        return identifier;
    }

   //public String getTermValue

}

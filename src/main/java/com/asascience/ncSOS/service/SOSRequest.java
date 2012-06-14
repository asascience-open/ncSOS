package thredds.server.sos.service;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author tkunicki
 */
public class SOSRequest {

    public enum Type {
        GetCapabilities,
        GetObservation,
        DescribeSensor;
    }
    
    public final String request;
    public final String service;
    public final String version;
    
    public SOSRequest(String request, String service, String version) {
        this.request = request;
        this.service = service;
        this.version = version;
    }

    public static SOSRequest parse(HttpServletRequest request) {
        String requestMethod = request.getMethod();
        if ("GET".equalsIgnoreCase(requestMethod)) {
            return parseGET(request);
        } else if ("POST".equalsIgnoreCase(requestMethod)) {
            return parsePOST(request);
        } else {
            throw new IllegalArgumentException("Unsupported REQUEST_METHOD of " + requestMethod);
        }
    }
    
    private static SOSRequest parseGET(HttpServletRequest request) {
        return null;
    }
    
    private static SOSRequest parsePOST(HttpServletRequest request) {
        return null;
    }
    
    
    
}

/*
* Access and use of this software shall impose the following
* obligations and understandings on the user. The user is granted the
* right, without any fee or cost, to use, copy, modify, alter, enhance
* and distribute this software, and any derivative works thereof, and
* its supporting documentation for any purpose whatsoever, provided
* that this entire notice appears in all copies of the software,
* derivative works and supporting documentation. Further, the user
* agrees to credit NOAA/NGDC in any publications that result from
* the use of this software or in any product that includes this
* software. The names NOAA/NGDC, however, may not be used
* in any advertising or publicity to endorse or promote any products
* or commercial entity unless specific written permission is obtained
* from NOAA/NGDC. The user also understands that NOAA/NGDC
* is not obligated to provide the user with any support, consulting,
* training or assistance of any kind with regard to the use, operation
* and performance of this software nor to provide the user with any
* updates, revisions, new versions or "bug fixes".
*
* THIS SOFTWARE IS PROVIDED BY NOAA/NGDC "AS IS" AND ANY EXPRESS
* OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL NOAA/NGDC BE LIABLE FOR ANY SPECIAL,
* INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER
* RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF
* CONTRACT, NEGLIGENCE OR OTHER TORTUOUS ACTION, ARISING OUT OF OR IN
* CONNECTION WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE. 
 */
package thredds.server.metadata.util;

import thredds.server.metadata.bean.Extent;
import thredds.server.metadata.bean.NetCDFAttribute;
import thredds.server.metadata.bean.NetCDFAttributeType;
import thredds.server.metadata.bean.NetCDFAttributes;

import java.util.Hashtable;
import java.util.List;

import org.jdom.Element;
import org.jdom.Attribute;
import org.jdom.Namespace;

/**
* NCMLModifier
* @author: dneufeld
* Date: Jun 6, 2010
*/
public class NCMLModifier {

	private Hashtable<String, NetCDFAttribute> _netCDFAttHt = null;	

   /** 
	* Class constructor.
	*/ 	
	public NCMLModifier() {
		NetCDFAttributes netCDFAtt = new NetCDFAttributes();
		_netCDFAttHt = netCDFAtt.getHashtable();
	}
	
	/** 
	* Look for existing Data Discovery convention elements in the NCML document.
	* 
	* @param list list of NCML elements
	*/			
	public void analyze(final List<Element> childElems) {
		
	    for (Element childElem : childElems) {
	      List<Attribute> atts = childElem.getAttributes();
	      for (Attribute att : atts) {
	    	  if (att.getValue().equals("geospatial_lat_min")) _netCDFAttHt.put("geospatial_lat_min", new NetCDFAttribute(NetCDFAttributeType.GEOSPATIAL_LON_MIN, new Boolean(true)));	    	  
	    	  if (att.getValue().equals("geospatial_lat_max")) _netCDFAttHt.put("geospatial_lat_max", new NetCDFAttribute(NetCDFAttributeType.GEOSPATIAL_LAT_MIN, new Boolean(true)));

	    	  if (att.getValue().equals("geospatial_lon_min")) _netCDFAttHt.put("geospatial_lon_min", new NetCDFAttribute(NetCDFAttributeType.GEOSPATIAL_LON_MAX, new Boolean(true)));
	    	  if (att.getValue().equals("geospatial_lon_max")) _netCDFAttHt.put("geospatial_lon_max", new NetCDFAttribute(NetCDFAttributeType.GEOSPATIAL_LAT_MAX, new Boolean(true)));
	    	  
	    	  if (att.getValue().equals("geospatial_lat_units")) _netCDFAttHt.put("geospatial_lat_units", new NetCDFAttribute(NetCDFAttributeType.GEOSPATIAL_LON_UNITS, new Boolean(true)));
	    	  if (att.getValue().equals("geospatial_lat_resolution")) _netCDFAttHt.put("geospatial_lat_resolution", new NetCDFAttribute(NetCDFAttributeType.GEOSPATIAL_LON_RES, new Boolean(true)));
	    	  if (att.getValue().equals("geospatial_lon_units")) _netCDFAttHt.put("geospatial_lon_units", new NetCDFAttribute(NetCDFAttributeType.GEOSPATIAL_LAT_UNITS, new Boolean(true)));
	    	  if (att.getValue().equals("geospatial_lon_resolution")) _netCDFAttHt.put("geospatial_lon_resolution", new NetCDFAttribute(NetCDFAttributeType.GEOSPATIAL_LAT_RES, new Boolean(true)));
		 
	    	  if (att.getValue().equals("geospatial_vertical_min")) _netCDFAttHt.put("geospatial_vertical_min", new NetCDFAttribute(NetCDFAttributeType.GEOSPATIAL_VERTICAL_MIN, new Boolean(true)));
	    	  if (att.getValue().equals("geospatial_vertical_max")) _netCDFAttHt.put("geospatial_vertical_max", new NetCDFAttribute(NetCDFAttributeType.GEOSPATIAL_VERTICAL_MAX, new Boolean(true)));		    	  
	    	  if (att.getValue().equals("geospatial_vertical_units")) _netCDFAttHt.put("geospatial_vertical_units", new NetCDFAttribute(NetCDFAttributeType.GEOSPATIAL_VERTICAL_UNITS, new Boolean(true)));
	    	  if (att.getValue().equals("geospatial_vertical_resolution")) _netCDFAttHt.put("geospatial_vertical_resolution", new NetCDFAttribute(NetCDFAttributeType.GEOSPATIAL_VERTICAL_RES, new Boolean(true)));
	    	  if (att.getValue().equals("geospatial_vertical_positive")) _netCDFAttHt.put("geospatial_vertical_positive", new NetCDFAttribute(NetCDFAttributeType.GEOSPATIAL_VERTICAL_POS, new Boolean(true)));

	    	  if (att.getValue().equals("time_coverage_start")) _netCDFAttHt.put("time_coverage_start", new NetCDFAttribute(NetCDFAttributeType.TIME_COVERAGE_START, new Boolean(true)));
	    	  if (att.getValue().equals("time_coverage_end")) _netCDFAttHt.put("time_coverage_end", new NetCDFAttribute(NetCDFAttributeType.TIME_COVERAGE_END, new Boolean(true)));
	    	  if (att.getValue().equals("time_coverage_resolution")) _netCDFAttHt.put("time_coverage_resolution", new NetCDFAttribute(NetCDFAttributeType.TIME_COVERAGE_RES, new Boolean(true)));	    	  
	
	      }
	    }

	}
	
	/** 
	* Update the NCML document by calculating Data Discovery elements using CF conventions
	* wherever possible.
	* 
	* @param extent the geospatial extent of the NetCDF file
	* @param element the root XML element of the NCML document
	*/			
	public void update(final Extent ext, final Element rootElem) {
        for (String key : _netCDFAttHt.keySet()) {
        	NetCDFAttribute netCDFAtt = _netCDFAttHt.get(key);
        	//System.out.println(key + ": " + netCDFAtt.exists());
           
            if (!netCDFAtt.exists()) { // Add it
        	   switch (netCDFAtt.getType()) {
        	     //COORDS
        	     case GEOSPATIAL_LON_MIN:
        	    	 if (ext._minLon!=null) addElem(rootElem, key, ext._minLon.toString(), "float");
        	    	 break;
        	     case GEOSPATIAL_LAT_MIN:
        	    	 if (ext._minLat!=null) addElem(rootElem, key, ext._minLat.toString(), "float");
        	    	 break;        
        	     case GEOSPATIAL_LON_MAX:
        	    	 if (ext._maxLon!=null) addElem(rootElem, key, ext._maxLon.toString(), "float");
        	    	 break;
        	     case GEOSPATIAL_LAT_MAX:
        	    	 if (ext._maxLat!=null) addElem(rootElem, key, ext._maxLat.toString(), "float");
        	    	 break;  
        	     case GEOSPATIAL_LON_UNITS:
        	    	 if (ext._lonUnits!=null) addElem(rootElem, key, ext._lonUnits);
        	    	 break;       
        	     case GEOSPATIAL_LAT_UNITS:
        	    	 if (ext._latUnits!=null) addElem(rootElem, key, ext._latUnits);
        	    	 break;         	    	 
        	     case GEOSPATIAL_LON_RES:
        	    	 if (ext._lonRes!=null) addElem(rootElem, key, ext._lonRes.toString());
        	    	 break;
        	     case GEOSPATIAL_LAT_RES:
        	    	 if (ext._latRes!=null) addElem(rootElem, key, ext._latRes.toString());
        	    	 break;          	    	 
        	    	 
        	     //VERTICAL        	 		
        	     case GEOSPATIAL_VERTICAL_MIN:
        	    	 if (ext._minHeight!=null) addElem(rootElem, key, ext._minHeight.toString());
        	    	 break;
        	     case GEOSPATIAL_VERTICAL_MAX:
        	    	 if (ext._maxHeight!=null) addElem(rootElem, key, ext._maxHeight.toString());
        	    	 break;        	    	 
        	     case GEOSPATIAL_VERTICAL_UNITS:
        	    	 if (ext._heightUnits!=null) addElem(rootElem, key, ext._heightUnits);
        	    	 break;     
        	     case GEOSPATIAL_VERTICAL_RES:
        	    	 if (ext._heightRes!=null) addElem(rootElem, key, ext._heightRes.toString());
        	    	 break;      
        	     case GEOSPATIAL_VERTICAL_POS:
        	    	 if (ext._vOrientation!=null) addElem(rootElem, key, ext._vOrientation);
        	    	 break;         	    	 
        	     
        	     //TIME
        	     case TIME_COVERAGE_START:
        	    	 if (ext._minTime!=null) addElem(rootElem, key, ext._minTime.toString());
        	    	 break;  
        	     case TIME_COVERAGE_END:
        	    	 if (ext._maxTime!=null) addElem(rootElem, key, ext._maxTime.toString());
        	    	 break;       
        	     case TIME_COVERAGE_UNITS:
        	    	 if (ext._timeUnits!=null) addElem(rootElem, key, ext._timeUnits.toString());
        	    	 break;            	    	 
        	     case TIME_COVERAGE_RES:
        	    	 if (ext._timeRes!=null) addElem(rootElem, key, ext._timeRes.toString());
        	    	 break;         	    	 
        	   }
           }
		}
	}
	
	private Element newAttributeElement() {
		return new Element("attribute");
	}
	
	private void doAddElem(Element rootElem, final String name, final String value, final String type) {
		Namespace ns = Namespace.getNamespace("http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2");

		Element elem = newAttributeElement();
		elem.setAttribute("name", name);

		elem.setAttribute("value", value);
		if (type!=null) {
			elem.setAttribute("type", type);
		}
		elem.setNamespace(ns);
		rootElem.addContent(elem);
	}
	
	private void addElem(final Element rootElem, final String name, final String value) {
		doAddElem(rootElem, name, value, null);
	}
	
	private void addElem(final Element rootElem, final String name, final String value, final String type) {
		doAddElem(rootElem, name, value, type);
	}
}

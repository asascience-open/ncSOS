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
package thredds.server.metadata.bean;

import java.util.Hashtable;

/**
 * NetCDFAttributes
 * Author: dneufeld Date: Jun 14, 2010
 * <p/>
 */
public class NetCDFAttributes {

	private Hashtable<String, NetCDFAttribute> _netCDFAttHt = new Hashtable<String, NetCDFAttribute>();
    
	/** 
     * Class constructor.
     */
	public NetCDFAttributes() {
		_netCDFAttHt.put("geospatial_lon_min", new NetCDFAttribute(NetCDFAttributeType.GEOSPATIAL_LON_MIN, false));
		_netCDFAttHt.put("geospatial_lat_min", new NetCDFAttribute(NetCDFAttributeType.GEOSPATIAL_LAT_MIN, false));
		_netCDFAttHt.put("geospatial_lon_max", new NetCDFAttribute(NetCDFAttributeType.GEOSPATIAL_LON_MAX, false));
		_netCDFAttHt.put("geospatial_lat_max", new NetCDFAttribute(NetCDFAttributeType.GEOSPATIAL_LAT_MAX, false));
		
		_netCDFAttHt.put("geospatial_lon_units", new NetCDFAttribute(NetCDFAttributeType.GEOSPATIAL_LON_UNITS, false));
		_netCDFAttHt.put("geospatial_lat_units", new NetCDFAttribute(NetCDFAttributeType.GEOSPATIAL_LAT_UNITS, false));		
		_netCDFAttHt.put("geospatial_lon_resolution", new NetCDFAttribute(NetCDFAttributeType.GEOSPATIAL_LON_RES, false));
		_netCDFAttHt.put("geospatial_lat_resolution", new NetCDFAttribute(NetCDFAttributeType.GEOSPATIAL_LAT_RES, false));

		_netCDFAttHt.put("geospatial_vertical_min", new NetCDFAttribute(NetCDFAttributeType.GEOSPATIAL_VERTICAL_MIN, false));
		_netCDFAttHt.put("geospatial_vertical_max", new NetCDFAttribute(NetCDFAttributeType.GEOSPATIAL_VERTICAL_MAX, false));		
		_netCDFAttHt.put("geospatial_vertical_units", new NetCDFAttribute(NetCDFAttributeType.GEOSPATIAL_VERTICAL_UNITS, false));
		_netCDFAttHt.put("geospatial_vertical_resolution", new NetCDFAttribute(NetCDFAttributeType.GEOSPATIAL_VERTICAL_RES, false));
		_netCDFAttHt.put("geospatial_vertical_positive", new NetCDFAttribute(NetCDFAttributeType.GEOSPATIAL_VERTICAL_POS, false));
		
		_netCDFAttHt.put("time_coverage_start", new NetCDFAttribute(NetCDFAttributeType.TIME_COVERAGE_START, false));		
		_netCDFAttHt.put("time_coverage_end", new NetCDFAttribute(NetCDFAttributeType.TIME_COVERAGE_END, false));
		_netCDFAttHt.put("time_coverage_units", new NetCDFAttribute(NetCDFAttributeType.TIME_COVERAGE_UNITS, false));	
		_netCDFAttHt.put("time_coverage_resolution", new NetCDFAttribute(NetCDFAttributeType.TIME_COVERAGE_RES, false));		
	}
	
	/** 
	* Return the Hashtable containing NetCDFAttributes 
	* 
	* @return hashtable hashtable of netCDF attributes
	*/		
	public Hashtable<String, NetCDFAttribute> getHashtable() {
		return _netCDFAttHt;
	}
}

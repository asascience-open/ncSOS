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

/**
 * NetCDFAttributeType
 * Author: dneufeld Date: Jun 14, 2010
 * <p/>
 */
public enum NetCDFAttributeType {
	
	GEOSPATIAL_LAT_MIN,
	GEOSPATIAL_LAT_MAX,
	GEOSPATIAL_LON_MIN,
	GEOSPATIAL_LON_MAX,
	
	GEOSPATIAL_LAT_UNITS,
	GEOSPATIAL_LAT_RES,
	GEOSPATIAL_LON_UNITS,
	GEOSPATIAL_LON_RES,
	
	GEOSPATIAL_VERTICAL_MIN,
	GEOSPATIAL_VERTICAL_MAX,
	GEOSPATIAL_VERTICAL_UNITS,
	GEOSPATIAL_VERTICAL_RES,
	GEOSPATIAL_VERTICAL_POS,
	
	TIME_COVERAGE_START,
	TIME_COVERAGE_END,
	TIME_COVERAGE_UNITS,
	TIME_COVERAGE_DURATION,
	TIME_COVERAGE_RES
	
}

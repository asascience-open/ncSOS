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
package thredds.server.sos.util;

import java.util.List;

import thredds.server.sos.bean.Extent;
import org.apache.log4j.Logger;

import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.NetcdfDataset;

/**
* ThreddsExtentUtil
* @author: dneufeld
* Date: June 17, 2010
*/
public class ThreddsExtentUtil {
	private static final Logger _log = Logger
			.getLogger(ThreddsExtentUtil.class);
	
	private static Extent doGetExtent(final String url) throws Exception {
		Extent ext = null;

		try {
			NetcdfDataset ncd = NetcdfDataset.openDataset(url);
			ext = doGetExtent(ncd);
		} catch (Exception e) {
			e.printStackTrace();
			String err = "Could not load NETCDF file: " + url
					+ " because of Exception. " + e.getLocalizedMessage();
			_log.error(err, e);
		}
		return ext;
	}

	private static Extent doGetExtent(final NetcdfDataset ncd) throws Exception {
		Extent ext = new Extent();

		List<CoordinateAxis> coordAxes = ncd.getCoordinateAxes();
		try {
			for (CoordinateAxis coordAxis : coordAxes) {
				if (coordAxis.getAxisType() == AxisType.Lat) {
					ext._minLat = coordAxis.getMinValue();
					ext._maxLat = coordAxis.getMaxValue();
					ext._latUnits = coordAxis.getUnitsString();
					ext._latRes = ((coordAxis.getMaxValue() - coordAxis
							.getMinValue()) / coordAxis.getSize());
				}
				if (coordAxis.getAxisType() == AxisType.Lon) {
					ext._minLon = coordAxis.getMinValue();
					ext._maxLon = coordAxis.getMaxValue();
					ext._lonUnits = coordAxis.getUnitsString();
					ext._lonRes = ((coordAxis.getMaxValue() - coordAxis
							.getMinValue()) / coordAxis.getSize());
				}
				if (coordAxis.getAxisType() == AxisType.Time) {
					ext._minTime = Double.toString(coordAxis.getMinValue());
					ext._maxTime = Double.toString(coordAxis.getMaxValue());
					ext._timeUnits = coordAxis.getUnitsString();
					ext._timeRes = ((coordAxis.getMaxValue() - coordAxis
							.getMinValue()) / coordAxis.getSize());
				}
				if (coordAxis.getAxisType() == AxisType.Height) {
					ext._minHeight = coordAxis.getMinValue();
					ext._maxHeight = coordAxis.getMaxValue();
					ext._heightUnits = coordAxis.getUnitsString();
					ext._vOrientation = coordAxis.getPositive();
					ext._heightRes = ((coordAxis.getMaxValue() - coordAxis
							.getMinValue()) / coordAxis.getSize());
				}

			}
		} catch (Exception e) {
			_log.error(e.getMessage());
		}

		return ext;
	}

	/**
	 * Creates a spatial extent based upon
	 * a given url for a NetCDFDataset.
	 * 
	 * @param url
	 *            The fully qualified path to the netCDF file. Url must point to
	 *            a valid netCDF dataset.
	 * @return a spatial extent object
	 * @throws ThreddsUtilitiesException
	 */
	public static Extent getExtent(final String url) throws Exception {
		return doGetExtent(url);
	}

	/**
	 * Creates a spatial extent based upon
	 * a given NetcdfDataset.
	 * 
	 * @param ncd a valid netCDF dataset.
	 * @return a spatial extent object
	 * @throws ThreddsUtilitiesException
	 */
	public static Extent getExtent(final NetcdfDataset ncd) throws Exception {
		return doGetExtent(ncd);
	}	
}

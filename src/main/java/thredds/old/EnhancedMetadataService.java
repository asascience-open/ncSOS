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
package thredds.old;

import thredds.server.metadata.bean.Extent;
import thredds.server.metadata.util.ElementNameComparator;
import thredds.server.metadata.util.NCMLModifier;
import thredds.server.metadata.util.ThreddsExtentUtil;
import thredds.server.metadata.util.XMLUtil;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ncml.NcMLWriter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Writer;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;

/**
* EnhancedMetadataService
* @author: dneufeld
* Date: Jul 19, 2010
*/	
public class EnhancedMetadataService {
	private static final Logger _log = Logger.getLogger(EnhancedMetadataService.class);
	
	/** 
	* Enhance NCML with Data Discovery conventions elements if not already in place in the metadata.
	* 
	* @param dataset NetcdfDataset to enhance the NCML
	* @param writer writer to send enhanced NCML to
	*/	
	public static void enhance(final NetcdfDataset dataset,  final Writer writer) {
		Extent ext = null;

		try {
			ext = ThreddsExtentUtil.getExtent(dataset);
			
	        NcMLWriter ncMLWriter = new NcMLWriter();        	
		String ncml = ncMLWriter.writeXML(dataset);
		InputStream ncmlIs = new ByteArrayInputStream(ncml.getBytes("UTF-8"));
	    	XMLUtil xmlUtil = new XMLUtil(ncmlIs);
	    	List<Element> childElems = xmlUtil.elemFinder("//ncml:attribute", "ncml", "http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2");
	    	
	    	NCMLModifier ncmlMod = new NCMLModifier();
	    	ncmlMod.analyze(childElems);
	    	
	    	List<Element> list = xmlUtil.elemFinder("//ncml:netcdf", "ncml", "http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2");
	    	Element rootElem = list.get(0);
	   	
	    	ncmlMod.update(ext, rootElem);
	    	
		xmlUtil.sortElements(rootElem, new ElementNameComparator());
	    	xmlUtil.write(writer);				
		} catch (Exception e) {
			_log.error(e);
		}

		
	
	}
	
}

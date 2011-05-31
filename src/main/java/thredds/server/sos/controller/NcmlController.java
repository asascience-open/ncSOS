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
package thredds.server.sos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import thredds.old.EnhancedMetadataService;
import thredds.server.sos.util.DatasetHandlerAdapter;

import java.io.IOException;
import java.io.Writer;

import ucar.nc2.dataset.NetcdfDataset;

/**
 * Controller for NCML service 
 * Author: dneufeld Date: Jul 7, 2010
 * <p/>
 */
@Controller
@RequestMapping("/ncml")
public class NcmlController implements IMetadataContoller {
	private static org.slf4j.Logger _log = org.slf4j.LoggerFactory
			.getLogger(NcmlController.class);
	private static org.slf4j.Logger _logServerStartup = org.slf4j.LoggerFactory
			.getLogger("serverStartup");

	protected String getPath() {
		return "ncml/";
	}

	public void init() throws ServletException {
		_logServerStartup.info("Metadata Ncml - initialization start");

	}

	public void destroy() {
		NetcdfDataset.shutdown();
		_logServerStartup.info("Metadata Ncml - destroy done");
	}

	/** 
	* Generate NCML for the underlying NetcdfDataset
	* 
	* @param request incoming url request 
	* @param response outgoing web based response
	* @throws ServletException if ServletException occurred
	* @throws IOException if IOException occurred  
	*/	
	@RequestMapping(params = {})
	public void handleMetadataRequest(final HttpServletRequest req,
			final HttpServletResponse res) throws ServletException, IOException {
		_log.info("Handling NCML metadata request.");

		NetcdfDataset dataset = null;

		try {
			res.setContentType("text/xml");
			dataset = DatasetHandlerAdapter.openDataset(req, res);
			Writer writer = res.getWriter();
			EnhancedMetadataService.enhance(dataset, writer);
	        writer.flush();
	        writer.close();
	        
		} catch (Exception e) {
			_log.error(e.getMessage());

		} finally {
			DatasetHandlerAdapter.closeDataset(dataset);
		}
	}



}

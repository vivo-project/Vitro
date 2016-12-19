/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.imageprocessor.jai;

import edu.cornell.mannlib.vitro.webapp.imageprocessor.imageio.IIOImageProcessor;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.modules.imageProcessor.ImageProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Re-implemented as ImageIO / TwelveMonkeys plugin for better OpenJDK compatibility and to remove
 * JAI codec dependencies that are not distributed via Maven
 *
 * This transitional stub extends the new class, and logs warnings for people to update their config
 */
@Deprecated
public class JaiImageProcessor extends IIOImageProcessor {
	private static final Log log = LogFactory.getLog(JaiImageProcessor.class);

	@Deprecated
	@Override
	public void startup(Application application, ComponentStartupStatus ss) {
		log.warn("JaiImageProcessor is deprecated and will be removed - please update config/applicationSetup.n3 to use edu.cornell.mannlib.vitro.webapp.imageprocessor.imageio.IIOImageProcessor");
		super.startup(application, ss);
	}

	@Deprecated
	@Override
	public Dimensions getDimensions(InputStream imageStream) throws ImageProcessorException, IOException {
		log.warn("JaiImageProcessor is deprecated and will be removed - please update config/applicationSetup.n3 to use edu.cornell.mannlib.vitro.webapp.imageprocessor.imageio.IIOImageProcessor");
		return super.getDimensions(imageStream);
	}

	@Deprecated
	@Override
	public InputStream cropAndScale(InputStream mainImageStream, CropRectangle crop, Dimensions limits) throws ImageProcessorException, IOException {
		log.warn("JaiImageProcessor is deprecated and will be removed - please update config/applicationSetup.n3 to use edu.cornell.mannlib.vitro.webapp.imageprocessor.imageio.IIOImageProcessor");
		return super.cropAndScale(mainImageStream, crop, limits);
	}
}

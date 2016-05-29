/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.imageprocessor.imageio;

import com.sun.media.jai.codec.MemoryCacheSeekableStream;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.modules.imageProcessor.ImageProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.BandSelectDescriptor;
import javax.media.jai.operator.StreamDescriptor;
import javax.media.jai.util.ImagingListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Crop the main image as specified, and scale it to the correct size for a
 * thumbnail.
 * 
 * Use the JAI library to read the file because the javax.imageio package
 * doesn't read extended JPEG properly. Use JAI to remove transparency from
 * JPEGs and PNGs, simply by removing the alpha channel. Annoyingly, this will
 * not work with GIFs with transparent pixels.
 * 
 * The transforms in the JAI library are buggy, so standard AWT operations do
 * the scaling and cropping. The most obvious problem in the JAI library is the
 * refusal to crop after scaling an image.
 * 
 * Scale first to avoid the boundary error that produces black lines along the
 * edge of the image.
 * 
 * Use the javax.imagio pacakge to write the thumbnail image as a JPEG file.
 */
public class IIOImageProcessor implements ImageProcessor {
	private static final Log log = LogFactory.getLog(IIOImageProcessor.class);

	/** If an image has 3 color bands and 1 alpha band, we want these. */
	private static final int[] COLOR_BAND_INDEXES = new int[] { 0, 1, 2 };

	/**
	 * Prevent Java Advanced Imaging from complaining about the lack of
	 * accelerator classes.
	 */
	@Override
	public void startup(Application application, ComponentStartupStatus ss) {
		JAI.getDefaultInstance().setImagingListener(
				new NonNoisyImagingListener());
	}

	@Override
	public void shutdown(Application application) {
		// Nothing to tear down.
	}

	@Override
	public Dimensions getDimensions(InputStream imageStream) throws ImageProcessorException, IOException {
		MemoryCacheSeekableStream stream = new MemoryCacheSeekableStream(imageStream);
		BufferedImage image = ImageIO.read(stream);
		return new Dimensions(image.getWidth(), image.getHeight());
	}

	/**
	 * Crop the main image according to this rectangle, and scale it to the
	 * correct size for a thumbnail.
	 */
	@Override
	public InputStream cropAndScale(InputStream mainImageStream,
			CropRectangle crop, Dimensions limits)
			throws ImageProcessorException, IOException {
		try {
			MemoryCacheSeekableStream stream = new MemoryCacheSeekableStream(mainImageStream);
			BufferedImage mainImage = ImageIO.read(stream);

			BufferedImage bufferedImage = new BufferedImage(mainImage.getWidth(), mainImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR); // BufferedImage.TYPE_INT_RGB
			new ColorConvertOp(null).filter(mainImage, bufferedImage);

			log.debug("initial image: " + imageSize(bufferedImage));

			log.debug("initial crop: " + crop);
			CropRectangle boundedCrop = limitCropRectangleToImageBounds(
					bufferedImage, crop);
			log.debug("bounded crop: " + boundedCrop);

			float scaleFactor = figureScaleFactor(boundedCrop, limits);
			log.debug("scale factor: " + scaleFactor);

			BufferedImage scaledImage = scaleImage(bufferedImage, scaleFactor);
			log.debug("scaled image: " + imageSize(scaledImage));

			CropRectangle rawScaledCrop = adjustCropRectangleToScaledImage(
					boundedCrop, scaleFactor);
			log.debug("scaled crop: " + rawScaledCrop);
			CropRectangle scaledCrop = limitCropRectangleToImageBounds(
					scaledImage, rawScaledCrop);
			log.debug("bounded scaled crop: " + scaledCrop);

			BufferedImage croppedImage = cropImage(scaledImage, scaledCrop);
			log.debug("cropped image: " + imageSize(croppedImage));

			byte[] jpegBytes = encodeAsJpeg(croppedImage);
			return new ByteArrayInputStream(jpegBytes);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to scale the image", e);
		}
	}

	private String imageSize(BufferedImage image) {
		return image.getWidth() + " by " + image.getHeight();
	}

	private CropRectangle limitCropRectangleToImageBounds(BufferedImage image,
			CropRectangle crop) {

		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

		// Ensure that x and y are at least zero, but not big enough to push the
		// crop rectangle out of the image.
		int greatestX = imageWidth - MINIMUM_CROP_SIZE;
		int greatestY = imageHeight - MINIMUM_CROP_SIZE;
		int x = Math.max(0, Math.min(greatestX, Math.abs(crop.x)));
		int y = Math.max(0, Math.min(greatestY, Math.abs(crop.y)));

		// Ensure that width and height are at least as big as the minimum, but
		// no so big as to extend beyond the image.
		int greatestW = imageWidth - x;
		int greatestH = imageHeight - y;
		int w = Math.max(MINIMUM_CROP_SIZE, Math.min(greatestW, crop.width));
		int h = Math.max(MINIMUM_CROP_SIZE, Math.min(greatestH, crop.height));

		return new CropRectangle(x, y, h, w);
	}

	private float figureScaleFactor(CropRectangle boundedCrop, Dimensions limits) {
		float horizontalScale = ((float) limits.width)
				/ ((float) boundedCrop.width);
		float verticalScale = ((float) limits.height)
				/ ((float) boundedCrop.height);
		return Math.min(horizontalScale, verticalScale);
	}

	private BufferedImage scaleImage(BufferedImage image, float scaleFactor) {
		AffineTransform transform = AffineTransform.getScaleInstance(
				scaleFactor, scaleFactor);
		AffineTransformOp atoOp = new AffineTransformOp(transform, null);
		return atoOp.filter(image, null);
	}

	private CropRectangle adjustCropRectangleToScaledImage(CropRectangle crop,
			float scaleFactor) {
		int newX = (int) (crop.x * scaleFactor);
		int newY = (int) (crop.y * scaleFactor);
		int newHeight = (int) (crop.height * scaleFactor);
		int newWidth = (int) (crop.width * scaleFactor);
		return new CropRectangle(newX, newY, newHeight, newWidth);
	}

	private BufferedImage cropImage(BufferedImage image, CropRectangle crop) {
		return image.getSubimage(crop.x, crop.y, crop.width, crop.height);
	}

	private byte[] encodeAsJpeg(BufferedImage image) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ImageIO.write(image, "JPG", bytes);
		return bytes.toByteArray();
	}

	/**
	 * This ImagingListener means that Java Advanced Imaging won't dump an
	 * exception log to System.out. It writes to the log, instead.
	 * 
	 * Further, since the lack of native accelerator classes isn't an error, it
	 * is written as a simple log message.
	 */
	static class NonNoisyImagingListener implements ImagingListener {
		@Override
		public boolean errorOccurred(String message, Throwable thrown,
				Object where, boolean isRetryable) throws RuntimeException {
			if (thrown instanceof RuntimeException) {
				throw (RuntimeException) thrown;
			}
			if ((thrown instanceof NoClassDefFoundError)
					&& (thrown.getMessage()
							.contains("com/sun/medialib/mlib/Image"))) {
				log.info("Java Advanced Imaging: Could not find mediaLib "
						+ "accelerator wrapper classes. "
						+ "Continuing in pure Java mode.");
				return false;
			}
			log.error(thrown, thrown);
			return false;
		}

	}

}

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.BandSelectDescriptor;
import javax.media.jai.operator.StreamDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.media.jai.codec.MemoryCacheSeekableStream;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.ImageUploadController.CropRectangle;

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
public class ImageUploadThumbnailer {
	/** If an image has 3 color bands and 1 alpha band, we want these. */
	private static final int[] COLOR_BAND_INDEXES = new int[] { 0, 1, 2 };

	private static final Log log = LogFactory
			.getLog(ImageUploadThumbnailer.class);

	/** We won't let you crop to smaller than this many pixels wide or high. */
	private static final int MINIMUM_CROP_SIZE = 5;

	private final int thumbnailHeight;
	private final int thumbnailWidth;

	public ImageUploadThumbnailer(int thumbnailHeight, int thumbnailWidth) {
		this.thumbnailHeight = thumbnailHeight;
		this.thumbnailWidth = thumbnailWidth;
	}

	/**
	 * Crop the main image according to this rectangle, and scale it to the
	 * correct size for a thumbnail.
	 */
	public InputStream cropAndScale(InputStream mainImageStream,
			CropRectangle crop) {
		try {
			RenderedOp mainImage = loadImage(mainImageStream);
			RenderedOp opaqueImage = makeImageOpaque(mainImage);

			BufferedImage bufferedImage = opaqueImage.getAsBufferedImage();
			log.debug("initial image: " + imageSize(bufferedImage));

			log.debug("initial crop: " + crop);
			CropRectangle boundedCrop = limitCropRectangleToImageBounds(
					bufferedImage, crop);
			log.debug("bounded crop: " + boundedCrop);

			float scaleFactor = figureScaleFactor(boundedCrop);
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

	private RenderedOp loadImage(InputStream imageStream) {
		return StreamDescriptor.create(new MemoryCacheSeekableStream(
				imageStream), null, null);
	}

	private RenderedOp makeImageOpaque(RenderedOp image) {
		ColorModel colorModel = image.getColorModel();

		if (!colorModel.hasAlpha()) {
			// The image is already opaque.
			return image;
		}

		if (image.getNumBands() == 4) {
			// The image has a separate alpha channel. Drop the alpha channel.
			return BandSelectDescriptor.create(image, COLOR_BAND_INDEXES, null);
		}

		// Don't know how to handle it. Probably a GIF with a transparent
		// background. Give up.
		return image;
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

	private float figureScaleFactor(CropRectangle boundedCrop) {
		float horizontalScale = ((float) thumbnailWidth)
				/ ((float) boundedCrop.width);
		float verticalScale = ((float) thumbnailHeight)
				/ ((float) boundedCrop.height);
		return Math.min(horizontalScale, verticalScale);
	}

	private BufferedImage cropImage(BufferedImage image, CropRectangle crop) {
		return image.getSubimage(crop.x, crop.y, crop.width, crop.height);
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

	private byte[] encodeAsJpeg(BufferedImage image) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ImageIO.write(image, "JPG", bytes);
		return bytes.toByteArray();
	}
}

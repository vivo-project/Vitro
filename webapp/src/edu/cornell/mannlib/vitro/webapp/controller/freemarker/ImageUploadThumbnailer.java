/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.awt.image.ColorModel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.media.jai.InterpolationBilinear;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.BandSelectDescriptor;
import javax.media.jai.operator.CropDescriptor;
import javax.media.jai.operator.EncodeDescriptor;
import javax.media.jai.operator.ScaleDescriptor;
import javax.media.jai.operator.StreamDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.media.jai.codec.JPEGEncodeParam;
import com.sun.media.jai.codec.MemoryCacheSeekableStream;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.ImageUploadController.CropRectangle;

/**
 * Crop the main image as specified, and scale it to the correct size for a
 * thumbnail.
 * 
 * The JAI library has a problem when writing a JPEG from a source image with an
 * alpha channel (transparency). The colors come out inverted. We throw in a
 * step that will remove transparency from a PNG, but it won't touch a GIF.
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
			RenderedOp croppedImage = cropImage(opaqueImage, crop);
			RenderedOp scaledImage = scaleImage(croppedImage);
			byte[] jpegBytes = encodeAsJpeg(scaledImage);
			return new ByteArrayInputStream(jpegBytes);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to scale the image", e);
		}
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

	private RenderedOp cropImage(RenderedOp image, CropRectangle crop) {
		CropRectangle boundedCrop = limitCropRectangleToImageBounds(image, crop);
		return CropDescriptor.create(image, (float) boundedCrop.x,
				(float) boundedCrop.y, (float) boundedCrop.width,
				(float) boundedCrop.height, null);
	}

	private RenderedOp scaleImage(RenderedOp image) {
		float horizontalScale = ((float) thumbnailWidth)
				/ ((float) image.getWidth());
		float verticalScale = ((float) thumbnailHeight)
				/ ((float) image.getHeight());
		log.debug("Generating a thumbnail, scales: " + horizontalScale + ", "
				+ verticalScale);

		return ScaleDescriptor.create(image, horizontalScale, verticalScale,
				0.0F, 0.0F, new InterpolationBilinear(), null);
	}

	private byte[] encodeAsJpeg(RenderedOp image) throws IOException {
		JPEGEncodeParam encodeParam = new JPEGEncodeParam();
		encodeParam.setQuality(1.0F);

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		EncodeDescriptor.create(image, bytes, "JPEG", encodeParam, null);
		return bytes.toByteArray();
	}

	private CropRectangle limitCropRectangleToImageBounds(RenderedOp image,
			CropRectangle crop) {
		log.debug("Generating a thumbnail, initial crop info: " + crop);

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

		CropRectangle bounded = new CropRectangle(x, y, h, w);
		log.debug("Generating a thumbnail, bounded crop info: " + bounded);

		return bounded;
	}

}

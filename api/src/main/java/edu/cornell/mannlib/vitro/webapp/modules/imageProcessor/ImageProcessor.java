/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules.imageProcessor;

import java.io.IOException;
import java.io.InputStream;

import edu.cornell.mannlib.vitro.webapp.modules.Application;

/**
 * Handle image processing for VIVO; uploads and thumbnailing.
 */
public interface ImageProcessor extends Application.Module {

	/**
	 * We won't let you crop to smaller than this many pixels wide or high.
	 */
	public static final int MINIMUM_CROP_SIZE = 5;

	/**
	 * How big is the image contained in this stream?
	 * 
	 * @param image
	 *            The image stream. This method will not close it.
	 * @return The dimensions of the image. Never returns null.
	 * @throws ImageProcessorException
	 *             if the stream does not contain a valid image.
	 * @throws IOException
	 *             if the stream cannot be read.
	 */
	Dimensions getDimensions(InputStream image) throws ImageProcessorException,
			IOException;

	/**
	 * Create a new image from a portion of the supplied image, and reduce to
	 * fit a limiting size.
	 * 
	 * If the crop rectangle extends beyond the image boundaries, it is limited
	 * to fit, or relocated if limiting would make it smaller than the
	 * MINIMUM_CROP_SIZE.
	 * 
	 * @param image
	 *            The image stream. This method will not close it.
	 * @param crop
	 *            x and y determine the upper left corner of the crop area.
	 *            height and width determine the size of the crop area.
	 * @param limits
	 *            The resulting image will be reduced as necessary to fit within
	 *            these dimensions.
	 * @return The new image. Client code should close this stream after
	 *         reading. Never returns null.
	 * @throws ImageProcessorException
	 *             If the image is smaller than the minimum crop size, or if
	 *             there is another problem cropping the image.
	 * @throws IOException
	 *             if the image stream cannot be read.
	 */
	InputStream cropAndScale(InputStream image, CropRectangle crop,
			Dimensions limits) throws ImageProcessorException, IOException;

	/**
	 * A problem with the image.
	 */
	public static class ImageProcessorException extends Exception {
		public ImageProcessorException(String message) {
			super(message);
		}

		public ImageProcessorException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	/**
	 * The size of a rectangular image, in pixels.
	 */
	public static class Dimensions {
		public final int width;
		public final int height;

		public Dimensions(int width, int height) {
			this.width = width;
			this.height = height;
		}

		@Override
		public String toString() {
			return "ImageProcessor.Dimensions[width=" + width + ", height="
					+ height + "]";
		}
	}

	/**
	 * Holds the coordinates that we use to crop an image.
	 */
	public static class CropRectangle {
		public final int x;
		public final int y;
		public final int height;
		public final int width;

		public CropRectangle(int x, int y, int height, int width) {
			this.x = x;
			this.y = y;
			this.height = height;
			this.width = width;
		}

		@Override
		public String toString() {
			return "CropRectangle[x=" + x + ", y=" + y + ", w=" + width
					+ ", h=" + height + "]";
		}

	}

}

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.imageprocessor.jai;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.Raster;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.StreamDescriptor;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.sun.media.jai.codec.MemoryCacheSeekableStream;

import edu.cornell.mannlib.vitro.webapp.imageprocessor.jai.JaiImageProcessorTester2.CropDataSet.CropData;
import edu.cornell.mannlib.vitro.webapp.imageprocessor.jai.JaiImageProcessor.NonNoisyImagingListener;
import edu.cornell.mannlib.vitro.webapp.modules.imageProcessor.ImageProcessor.CropRectangle;
import edu.cornell.mannlib.vitro.webapp.modules.imageProcessor.ImageProcessor.Dimensions;

/**
 * This is not a unit test, so it is not named BlahBlahTest.
 * 
 * Instead, it's a test harness that creates thumbnails and displays them in a
 * window on the screen. It takes human intervention to evaluate.
 * 
 * The goal here is to see whether differences in crop dimensions might cause
 * one or more black edges on the thumbnails.
 */
@SuppressWarnings("deprecation")
public class JaiImageProcessorTester2 extends Frame {
	private static final Log log = LogFactory
			.getLog(JaiImageProcessorTester2.class);

	private static final int ROWS = 6;
	private static final int COLUMNS = 9;

	private static final int EDGE_THRESHOLD = 6000;
	
	private static final Dimensions THUMBNAIL_SIZE = new Dimensions(200, 200);

	/** Keep things quiet. */
	static {
		JAI.getDefaultInstance().setImagingListener(
				new NonNoisyImagingListener());
	}

	private final String imagePath;
	private final JaiImageProcessor thumbnailer;

	public JaiImageProcessorTester2(String imagePath,
			CropDataSet cropDataSet) {
		this.imagePath = imagePath;
		this.thumbnailer = new JaiImageProcessor();

		setTitle("Cropping edging test");
		addWindowListener(new CloseWindowListener());
		setLayout(new GridLayout(ROWS, COLUMNS));

		for (CropData cropData : cropDataSet.crops()) {
			add(createImagePanel(cropData));
		}

		pack();
		setVisible(true);
	}

	private Component createImagePanel(CropData cropData) {
		RenderedOp image = createCroppedImage(cropData);


		Set<String> blackSides = checkBlackEdges(image);
		if (!blackSides.isEmpty()) {
			log.warn("edges  at " + cropData + ", " + blackSides);
		}

		String legend = "left=" + cropData.left + ", top=" + cropData.top
				+ ", size=" + cropData.size;
		Label l = new Label();
		l.setAlignment(Label.CENTER);
		if (!blackSides.isEmpty()) {
			l.setBackground(new Color(0xFFDDDD));
			legend += " " + blackSides;
		}
		l.setText(legend);

		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add("South", l);
		p.add("Center", new javax.media.jai.widget.ImageCanvas(image));
		p.setBackground(new Color(0xFFFFFF));
		p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		return p;
	}

	private RenderedOp createCroppedImage(CropData cropData) {
		try {
			InputStream mainStream = new FileInputStream(imagePath);
			CropRectangle rectangle = new CropRectangle(cropData.left,
					cropData.top, cropData.size, cropData.size);
			InputStream thumbnailStream = thumbnailer.cropAndScale(mainStream,
					rectangle, THUMBNAIL_SIZE);

			return StreamDescriptor.create(new MemoryCacheSeekableStream(
					thumbnailStream), null, null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Set<String> checkBlackEdges(RenderedOp image) {
		Raster imageData = image.getData();

		int minX = imageData.getMinX();
		int minY = imageData.getMinY();
		int maxX = minX + imageData.getWidth() - 1;
		int maxY = minY + imageData.getHeight() - 1;

		Set<String> blackSides = new HashSet<String>();
		if (isBlackEdge(minX, minX, minY, maxY, imageData)) {
			blackSides.add("left");
		}
		if (isBlackEdge(minX, maxX, minY, minY, imageData)) {
			blackSides.add("top");
		}
		if (isBlackEdge(maxX, maxX, minY, maxY, imageData)) {
			blackSides.add("right");
		}
		if (isBlackEdge(minX, maxX, maxY, maxY, imageData)) {
			blackSides.add("bottom");
		}
		return blackSides;
	}

	private boolean isBlackEdge(int fromX, int toX, int fromY, int toY,
			Raster imageData) {
		int edgeTotal = 0;
		try {
			for (int col = fromX; col <= toX; col++) {
				for (int row = fromY; row <= toY; row++) {
					edgeTotal += sumPixel(imageData, col, row);
				}
			}
		} catch (Exception e) {
			log.error("can't sum edge: fromX=" + fromX + ", toX=" + toX
					+ ", fromY=" + fromY + ", toY=" + toY + ", imageWidth="
					+ imageData.getWidth() + ", imageHeight="
					+ imageData.getHeight() + ": " + e);
		}

		log.debug("edge total = " + edgeTotal);
		return edgeTotal < EDGE_THRESHOLD;
	}

	private int sumPixel(Raster imageData, int col, int row) {
		int pixelSum = 0;
		int[] pixel = imageData.getPixel(col, row, new int[0]);
		for (int value : pixel) {
			pixelSum += value;
		}
		return pixelSum;
	}

	/**
	 * <pre>
	 * The plan:
	 * 
	 * Provide the path to an image file.
	 * Figure how many images can fit on the screen.
	 * Crop in increments, starting at 0,0 and varying the size of the crop.
	 * Crop in increments, incrementing from 0,0 downward, and varying the size of the crop.
	 * 
	 * Start by creating 4 x 4 images in a window, and incrementing from 201 to 216.
	 * </pre>
	 */

	public static void main(String[] args) {
		Logger rootLogger = Logger.getRootLogger();
		Appender appender = (Appender) rootLogger.getAllAppenders()
				.nextElement();
		appender.setLayout(new PatternLayout("%-5p [%c{1}] %m%n"));

		Logger.getLogger(JaiImageProcessor.class).setLevel(Level.DEBUG);
		Logger.getLogger(JaiImageProcessorTester2.class).setLevel(
				Level.INFO);

		CropDataSet cropDataSet = new CropDataSet();
		for (int i = 0; i < ROWS * COLUMNS; i++) {
//			cropDataSet.add(i, i, 201 + i);
			cropDataSet.add(0, 0, 201 + i);
		}

		new JaiImageProcessorTester2(
				"C:/Users/jeb228/Pictures/wheel.png", cropDataSet);

//		new ImageUploaderThumbnailerTester_2(
//				"C:/Users/jeb228/Pictures/DSC04203w-trans.jpg", cropDataSet);

		//		new ImageUploaderThumbnailerTester_2(
//				"C:/Development/JIRA issues/NIHVIVO-2477 Black borders on thumbnails/"
//				+ "images from Alex/uploads/file_storage_root/a~n/411/9/"
//				+ "De^20Bartolome^2c^20Charles^20A^20M_100037581.jpg",
//				cropDataSet);
	}

	// ----------------------------------------------------------------------
	// helper classes
	// ----------------------------------------------------------------------

	private class CloseWindowListener extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			setVisible(false);
			dispose();
			System.exit(0);
		}
	}

	public static class CropDataSet {
		private final List<CropData> crops = new ArrayList<CropData>();

		CropDataSet add(int left, int top, int size) {
			crops.add(new CropData(left, top, size));
			return this;
		}

		Collection<CropData> crops() {
			return Collections.unmodifiableCollection(crops);
		}

		public static class CropData {
			final int left;
			final int top;
			final int size;

			CropData(int left, int top, int size) {
				this.left = left;
				this.top = top;
				this.size = size;
			}

			@Override
			public String toString() {
				return "CropData[" + left + ", " + top + ", " + size + "]";
			}
		}
	}
}

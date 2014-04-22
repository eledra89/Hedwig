package fi.aalto.cse.harry.worker;


import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fi.aalto.cse.harry.imageprocessing.CircleRecognizer;
import fi.aalto.cse.harry.structure.ImageQueue;
import fi.aalto.cse.harry.structure.RectangleDimensionsQueue;
import fi.aalto.cse.harry.structure.RectanleDimensions;

public class CircleRecognitionExecutor {

	private static CircleRecognitionExecutor INSTANCE;

	private static final int NO_THREADS = 1;

	private CircleRecognitionExecutor() {
		ExecutorService executor = Executors.newFixedThreadPool(NO_THREADS);
		for (int i = 0; i < NO_THREADS; i++) {
			executor.execute(new CircleRecognitionRunnable());
		}
	}

	public static void initialize() {
		synchronized (CircleRecognitionExecutor.class) {
			if (INSTANCE == null) {
				synchronized (CircleRecognitionExecutor.class) {
					INSTANCE = new CircleRecognitionExecutor();
				}
			}
		}
	}

	/**
	 * Peeks image from ImageQueue, apply face detection algorithm on it. Face
	 * detection algorithm returns list of rectangle dimensions on the image
	 * where faces are detected. List of rectangle dimensions is store in
	 * {@link #RectangleDimensionsQueue}. List of rectangle dimensions is then
	 * accessed by {@link #DisplayImageInPanelExecutor} to render image with
	 * rectangles.
	 * 
	 * 
	 */
	private class CircleRecognitionRunnable implements Runnable {
		private CircleRecognizer circleRecognizer;

		public CircleRecognitionRunnable() {
			circleRecognizer = new CircleRecognizer();
		}

		@Override
		public void run() {
			while (true) {
				if (isEmpty()) {
					sleepTime();
				} else {
					BufferedImage buf = getImageFromQueue();
					if (buf != null) {
						// System.out.println("Face detection in progress.");
						List<RectanleDimensions> rectDimensionsList = circleRecognizer
								.recognize(buf);
						// Add dimension to queue.
						RectangleDimensionsQueue.getInstance()
								.addRectangleDimensions(rectDimensionsList);
					}
				}
			}
		}
		
		private void sleepTime() {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	// It does not remove image from the image queue just get the top most image
	// buffer.
	private BufferedImage getImageFromQueue() {
		ImageQueue imageQueue = ImageQueue.getInstance();
		return imageQueue.peekImage();
	}

	private boolean isEmpty() {
		ImageQueue imageQueue = ImageQueue.getInstance();
		return imageQueue.isEmpty();
	}
}
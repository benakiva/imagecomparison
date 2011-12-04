package net.avh4.util.imagecomparison;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

public class ImageComparison {

	private static final List<Renderer> renderers = Arrays.asList(
			new SwingRenderer(), new UILayerRenderer());

	public static boolean matchesImage(final BufferedImage itemImage,
			final BufferedImage referenceImage, final String filename) {
		// Compare the image sizes
		if (itemImage.getWidth() != referenceImage.getWidth()
				|| itemImage.getHeight() != referenceImage.getHeight()) {
			write(itemImage, filename);
			return false;
		}

		// Compare the image data
		final Raster itemRaster = itemImage.getData();
		final Raster referenceRaster = referenceImage.getData();
		final int width = itemRaster.getWidth();
		final int height = itemRaster.getHeight();
		for (int y = 0; y < height; y++) {
			final int itemPixels[] = new int[4 * width];
			final int referencePixels[] = new int[4 * width];
			itemRaster.getPixels(0, y, width, 1, itemPixels);
			referenceRaster.getPixels(0, y, width, 1, referencePixels);
			for (int i = 0; i < 4 * width; i++) {
				if (itemPixels[i] != referencePixels[i]) {
					write(itemImage, filename);
					return false;
				}
			}
		}

		return true;
	}

	private static BufferedImage read(final String imageName) {
		try {
			return ImageIO.read(new File(imageName));
		} catch (final IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static void write(final BufferedImage image, final String filename) {
		try {
			ImageIO.write(image, "png", new File(filename));
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean matches(final Object item,
			final String referenceFilename, final String outputFilename) {
		final BufferedImage expectedImage = read(referenceFilename);
		return matches(item, expectedImage, outputFilename);
	}

	public static boolean matches(final Object actual,
			final BufferedImage expectedImage, final String outputFilename) {
		final BufferedImage actualImage = ImageComparison.getImage(actual);
		if (actualImage == null) {
			return false;
		} else if (expectedImage == null) {
			write(actualImage, outputFilename);
			return false;
		} else {
			return matchesImage(actualImage, expectedImage, outputFilename);
		}
	}

	private static BufferedImage getImage(final Object item) {

		if (item instanceof BufferedImage) {
			return (BufferedImage) item;
		}

		for (final Renderer r : renderers) {
			final BufferedImage rendering = r.getImage(item);
			if (rendering != null) {
				return rendering;
			}
		}

		throw new RuntimeException(String.format(
				"Don't know how to make an image of %s\nUsing renderers %s",
				item.toString(), renderers.toString()));
	}
}
package de.mpg.mis.neuesbibliothekssystem.stacks.services;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

public class ImageService {

    // Thanks to
    // http://stackoverflow.com/questions/672916/how-to-get-image-height-and-width-using-java/2911772#2911772
    public static Dimension getUprightImageDim(final File image) {
	Dimension result = null;
	String suffix = getFileSuffix(image.getAbsolutePath());
	Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);
	if (iter.hasNext()) {
	    ImageReader reader = iter.next();
	    try {
		ImageInputStream stream = new FileImageInputStream(image);
		reader.setInput(stream);
		int width = reader.getWidth(reader.getMinIndex());
		int height = reader.getHeight(reader.getMinIndex());
		if (width > height)
		    result = new Dimension(height, width);
		else
		    result = new Dimension(width, height);
	    } catch (IOException e) {
		System.err.println(e.getMessage());
	    } finally {
		reader.dispose();
	    }
	} else {
	    System.err.println("No reader found for given format: " + suffix);
	}
	return result;
    }

    public static String getFileSuffix(final String path) {
	String result = null;
	if (path != null) {
	    result = "";
	    if (path.lastIndexOf('.') != -1) {
		result = path.substring(path.lastIndexOf('.'));
		if (result.startsWith(".")) {
		    result = result.substring(1);
		}
	    }
	}
	return result;
    }

}

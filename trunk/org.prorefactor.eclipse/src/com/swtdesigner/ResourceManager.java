package com.swtdesigner;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * Utility class for managing OS resources associated with SWT controls such as
 * colors, fonts, images, etc.
 * 
 * !!! IMPORTANT !!! Application code must explicitly invoke the <code>dispose()</code>
 * method to release the operating system resources managed by cached objects
 * when those objects and OS resources are no longer needed (e.g. on
 * application shutdown)
 * 
 * This class may be freely distributed as part of any application or plugin.
 * <p>
 * Copyright (c) 2003, Instantiations, Inc. <br>All Rights Reserved
 * 
 * @version $Revision: 1.10 $
 * @author scheglov_ke
 * @author Dan Rubel
 */
public class ResourceManager {

	/**
	 * Dispose of cached objects and their underlying OS resources. This should
	 * only be called when the cached objects are no longer needed (e.g. on
	 * application shutdown)
	 */
	public static void dispose() {
		disposeColors();
		disposeFonts();
		disposeImages();
		disposeCursors();
	}

	// Color support
	private static HashMap m_ColorMap = new HashMap();
	public static Color getColor(int systemColorID) {
		Display display = Display.getCurrent();
		return display.getSystemColor(systemColorID);
	}
	public static Color getColor(int r, int g, int b) {
		return getColor(new RGB(r, g, b));
	}
	public static Color getColor(RGB rgb) {
		Color color = (Color) m_ColorMap.get(rgb);
		if (color == null) {
			Display display = Display.getCurrent();
			color = new Color(display, rgb);
			m_ColorMap.put(rgb, color);
		}
		return color;
	}
	public static void disposeColors() {
		for (Iterator iter = m_ColorMap.values().iterator(); iter.hasNext();)
			 ((Color) iter.next()).dispose();
		m_ColorMap.clear();
	}

	// Image support
	private static HashMap m_ClassImageMap = new HashMap();
	private static HashMap m_DescriptorImageMap = new HashMap();
	private static HashMap m_ImageToDecoratorMap = new HashMap();
	private static Image getImage(InputStream is) {
		Display display = Display.getCurrent();
		ImageData data = new ImageData(is);
		if (data.transparentPixel > 0)
			return new Image(display, data, data.getTransparencyMask());
		return new Image(display, data);
	}
	public static Image getImage(String path) {
		String key = ResourceManager.class.getName() + "|" + path;
		Image image = (Image) m_ClassImageMap.get(key);
		if (image == null) {
			try {
				FileInputStream fis = new FileInputStream(path);
				image = getImage(fis);
				m_ClassImageMap.put(key, image);
				fis.close();
			} catch (Exception e) {
				return null;
			}
		}
		return image;
	}
	public static Image getImage(Class clazz, String path) {
		String key = clazz.getName() + "|" + path;
		Image image = (Image) m_ClassImageMap.get(key);
		if (image == null) {
			if (path.length() > 0 && path.charAt(0) == '/') {
				String newPath = path.substring(1, path.length());
				image = getImage(clazz.getClassLoader().getResourceAsStream(newPath));
			} else {
				image = getImage(clazz.getResourceAsStream(path));
			}
			m_ClassImageMap.put(key, image);
		}
		return image;
	}
	public static ImageDescriptor getImageDescriptor(Class clazz, String path) {
		return ImageDescriptor.createFromFile(clazz, path);
	}
	public static ImageDescriptor getImageDescriptor(String path) {
		try {
			return ImageDescriptor.createFromURL((new File(path)).toURL());
		} catch (MalformedURLException e) {
			return null;
		}
	}
	public static Image getImage(ImageDescriptor descriptor) {
		if (descriptor == null) return null;
		Image image = (Image) m_DescriptorImageMap.get(descriptor);
		if (image == null) {
			image = descriptor.createImage();
			m_DescriptorImageMap.put(descriptor, image);
		}
		return image;
	}
	public static Image decorateImage(Image baseImage, Image decorator) {
		HashMap decoratedMap = (HashMap) m_ImageToDecoratorMap.get(baseImage);
		if (decoratedMap == null) {
			decoratedMap = new HashMap();
			m_ImageToDecoratorMap.put(baseImage, decoratedMap);
		}
		Image result = (Image) decoratedMap.get(decorator);
		if (result == null) {
			ImageData bid = baseImage.getImageData();
			ImageData did = decorator.getImageData();
			result = new Image(Display.getCurrent(), bid.width, bid.height);
			GC gc = new GC(result);
			//
			gc.drawImage(baseImage, 0, 0);
			gc.drawImage(decorator, bid.width - did.width - 1, bid.height - did.height - 1);
			//
			gc.dispose();
			decoratedMap.put(decorator, result);
		}
		return result;
	}
	public static void disposeImages() {
		for (Iterator iter = m_ClassImageMap.values().iterator(); iter.hasNext();)
			 ((Image) iter.next()).dispose();
		m_ClassImageMap.clear();
		for (Iterator iter = m_DescriptorImageMap.values().iterator(); iter.hasNext();)
			 ((Image) iter.next()).dispose();
		m_DescriptorImageMap.clear();
	}

	// Plugin images support
	private static HashMap m_URLImageMap = new HashMap();
	public static Image getPluginImage(Object plugin, String name) {
		try {
			try {
				URL url = getPluginImageURL(plugin, name);
				if (m_URLImageMap.containsKey(url))
					return (Image) m_URLImageMap.get(url);
				InputStream is = url.openStream();
				Image image;
				try {
					image = getImage(is);
					m_URLImageMap.put(url, image);
				} finally {
					is.close();
				}
				return image;
			} catch (Throwable e) {
			}
		} catch (Throwable e) {
		}
		return null;
	}
	public static ImageDescriptor getPluginImageDescriptor(Object plugin, String name) {
		try {
			try {
				URL url = getPluginImageURL(plugin, name);
				return ImageDescriptor.createFromURL(url);
			} catch (Throwable e) {
			}
		} catch (Throwable e) {
		}
		return null;
	}
	private static URL getPluginImageURL(Object plugin, String name) throws Exception {
		Class pluginClass = Class.forName("org.eclipse.core.runtime.Plugin");
		Method getDescriptorMethod = pluginClass.getMethod("getDescriptor", null);
		Class pluginDescriptorClass = Class.forName("org.eclipse.core.runtime.IPluginDescriptor");
		Method getInstallURLMethod = pluginDescriptorClass.getMethod("getInstallURL", null);
		//
		Object pluginDescriptor = getDescriptorMethod.invoke(plugin, null);
		URL installURL = (URL) getInstallURLMethod.invoke(pluginDescriptor, null);
		URL url = new URL(installURL, name);
		return url;
	}
	
	// Font support
	private static HashMap m_FontMap = new HashMap();
	private static HashMap m_FontToBoldFontMap = new HashMap();
	public static Font getFont(String name, int height, int style) {
		String fullName = name + "|" + height + "|" + style;
		Font font = (Font) m_FontMap.get(fullName);
		if (font == null) {
			font = new Font(Display.getCurrent(), name, height, style);
			m_FontMap.put(fullName, font);
		}
		return font;
	}
	public static Font getBoldFont(Font baseFont) {
		Font font = (Font) m_FontToBoldFontMap.get(baseFont);
		if (font == null) {
			FontData fontDatas[] = baseFont.getFontData();
			FontData data = fontDatas[0];
			font = new Font(Display.getCurrent(), data.getName(), data.getHeight(), SWT.BOLD);
			m_FontToBoldFontMap.put(baseFont, font);
		}
		return font;
	}
	public static void disposeFonts() {
		for (Iterator iter = m_FontMap.values().iterator(); iter.hasNext();)
			 ((Font) iter.next()).dispose();
		m_FontMap.clear();
	}

	// CoolBar support
	public static void fixCoolBarSize(CoolBar bar) {
		CoolItem[] items = bar.getItems();
		// ensure that each item has control (at least empty one)
		for (int i = 0; i < items.length; i++) {
			CoolItem item = items[i];
			if (item.getControl() == null)
				item.setControl(new Canvas(bar, SWT.NONE) {
				public Point computeSize(int wHint, int hHint, boolean changed) {
					return new Point(20, 20);
				}
			});
		}
		// compute size for each item
		for (int i = 0; i < items.length; i++) {
			CoolItem item = items[i];
			Control control = item.getControl();
			control.pack();
			Point size = control.getSize();
			item.setSize(item.computeSize(size.x, size.y));
		}
	}

	// Cursor support
	private static HashMap m_IdToCursorMap = new HashMap();
	public static Cursor getCursor(int id) {
		Integer key = new Integer(id);
		Cursor cursor = (Cursor) m_IdToCursorMap.get(key);
		if (cursor == null) {
			cursor = new Cursor(Display.getDefault(), id);
			m_IdToCursorMap.put(key, cursor);
		}
		return cursor;
	}
	public static void disposeCursors() {
		for (Iterator iter = m_IdToCursorMap.values().iterator(); iter.hasNext();)
			 ((Cursor) iter.next()).dispose();
		m_IdToCursorMap.clear();
	}
}
/**
 * Copyright @ 2012 Quan Nguyen
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.sourceforge.tess4j;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sourceforge.tess4j.util.ImageIOHelper;

import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.PointerByReference;

/**
 * An object layer on top of <code>TessAPI1</code>, provides character
 * recognition support for common image formats, and multi-page TIFF images
 * beyond the uncompressed, binary TIFF format supported by Tesseract OCR
 * engine. The extended capabilities are provided by the
 * <code>Java Advanced Imaging Image I/O Tools</code>.<br>
 * <br>
 * Support for PDF documents is available through <code>Ghost4J</code>, a
 * <code>JNA</code> wrapper for <code>GPL Ghostscript</code>, which should be
 * installed and included in system path.<br>
 * <br>
 * Any program that uses the library will need to ensure that the required
 * libraries (the <code>.jar</code> files for <code>jna</code>,
 * <code>jai-imageio</code>, and <code>ghost4j</code>) are in its compile and
 * run-time <code>classpath</code>.
 */
public class Tesseract1 extends TessAPI1 implements ITesseract {

    private String language = "eng";
    private String datapath = "./";
    private RenderedFormat renderedFormat = RenderedFormat.TEXT;
    private int psm = -1;
    private int ocrEngineMode = TessOcrEngineMode.OEM_DEFAULT;
    private final Properties prop = new Properties();
    private final List<String> configList = new ArrayList<String>();

    private TessBaseAPI handle;

    private final static Logger logger = Logger.getLogger(Tesseract1.class.getName());

    /**
     * Returns API handle.
     *
     * @return handle
     */
    protected TessBaseAPI getHandle() {
        return handle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDatapath(String datapath) {
        this.datapath = datapath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOcrEngineMode(int ocrEngineMode) {
        this.ocrEngineMode = ocrEngineMode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPageSegMode(int mode) {
        this.psm = mode;
    }

    /**
     * Enables hocr output.
     *
     * @param hocr to enable or disable hocr output
     */
    public void setHocr(boolean hocr) {
        this.renderedFormat = hocr ? RenderedFormat.HOCR : RenderedFormat.TEXT;
        prop.setProperty("tessedit_create_hocr", hocr ? "1" : "0");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTessVariable(String key, String value) {
        prop.setProperty(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConfigs(List<String> configs) {
        configList.clear();
        if (configs != null) {
            configList.addAll(configs);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String doOCR(BufferedImage bi) throws TesseractException {
        return doOCR(bi, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String doOCR(BufferedImage bi, Rectangle rect) throws TesseractException {
        try {
            return doOCR(Arrays.asList(bi), rect);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new TesseractException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String doOCR(List<BufferedImage> imageList, Rectangle rect) throws TesseractException {
		return doOCR(imageList, null, rect);
    	 
    }
    
    /**
     * {@inheritDoc}
     */
	@Override
	public String doOCR(List<BufferedImage> imageList, String filename,
			Rectangle rect) throws TesseractException {
		init();
        setTessVariables();

        try {
            StringBuilder sb = new StringBuilder();
            int pageNum = 0;

            for (BufferedImage img : imageList) {
                pageNum++;
                setImage(img, rect);
				 sb.append(getOCRText(filename, pageNum));
            }

            if (renderedFormat == RenderedFormat.HOCR) {
                sb.insert(0, htmlBeginTag).append(htmlEndTag);
            }

            return sb.toString();
        } finally {
            dispose();
        }
	}

	/**
     * {@inheritDoc}
     */
    @Override
    public String doOCR(int xsize, int ysize, ByteBuffer buf, Rectangle rect, int bpp) throws TesseractException {
        return doOCR(xsize, ysize, buf, null, rect, bpp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String doOCR(int xsize, int ysize, ByteBuffer buf, String filename, Rectangle rect, int bpp) throws TesseractException {
        init();
        setTessVariables();

        try {
            setImage(xsize, ysize, buf, rect, bpp);
            return getOCRText(filename, 1);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new TesseractException(e);
        } finally {
            dispose();
        }
    }

    /**
     * Initializes Tesseract engine.
     */
    protected void init() {
        handle = TessBaseAPICreate();
        StringArray sarray = new StringArray(configList.toArray(new String[0]));
        PointerByReference configs = new PointerByReference();
        configs.setPointer(sarray);
        TessBaseAPIInit1(handle, datapath, language, ocrEngineMode, configs, configList.size());
        if (psm > -1) {
            TessBaseAPISetPageSegMode(handle, psm);
        }
    }

    /**
     * Sets Tesseract's internal parameters.
     */
    protected void setTessVariables() {
        Enumeration<?> em = prop.propertyNames();
        while (em.hasMoreElements()) {
            String key = (String) em.nextElement();
            TessBaseAPISetVariable(handle, key, prop.getProperty(key));
        }
    }

    /**
     * Sets image to be processed.
     *
     * @param buf buffered image
     * @param rect the bounding rectangle defines the region of the image to be
     * recognized. A rectangle of zero dimension or <code>null</code> indicates
     * the whole image.
     */
    protected void setImage(BufferedImage buf, Rectangle rect) {
    	setImage(buf.getWidth(), buf.getHeight(), ImageIOHelper.convertImageData(buf), rect, buf.getSampleModel().getSampleSize(0));
    }
    
    /**
     * Sets image to be processed.
     *
     * @param xsize width of image
     * @param ysize height of image
     * @param buf pixel data
     * @param rect the bounding rectangle defines the region of the image to be
     * recognized. A rectangle of zero dimension or <code>null</code> indicates
     * the whole image.
     * @param bpp bits per pixel, represents the bit depth of the image, with 1
     * for binary bitmap, 8 for gray, and 24 for color RGB.
     */
    protected void setImage(int xsize, int ysize, ByteBuffer buf, Rectangle rect, int bpp) {
        int bytespp = bpp / 8;
        int bytespl = (int) Math.ceil(xsize * bpp / 8.0);
        TessBaseAPISetImage(handle, buf, xsize, ysize, bytespp, bytespl);

        if (rect != null && !rect.isEmpty()) {
            TessBaseAPISetRectangle(handle, rect.x, rect.y, rect.width, rect.height);
        }
    }

    /**
     * Gets recognized text.
     *
     * @param filename input file name. Needed only for reading a UNLV zone
     * file.
     * @param pageNum page number; needed for hocr paging.
     * @return the recognized text
     */
    protected String getOCRText(String filename, int pageNum) {
        if (filename != null && !filename.isEmpty()) {
            TessBaseAPISetInputName(handle, filename);
        }

        Pointer utf8Text = renderedFormat == RenderedFormat.HOCR ? TessBaseAPIGetHOCRText(handle, pageNum - 1) : TessBaseAPIGetUTF8Text(handle);
        String str = utf8Text.getString(0);
        TessDeleteText(utf8Text);
        return str;
    }

    /**
     * Creates renderers for given formats.
     *
     * @param outputbase
     * @param formats
     * @return
     */
    private TessResultRenderer createRenderers(String outputbase, List<RenderedFormat> formats) {
        TessResultRenderer renderer = null;

        for (RenderedFormat format : formats) {
            switch (format) {
                case TEXT:
                    if (renderer == null) {
                        renderer = TessTextRendererCreate(outputbase);
                    } else {
                        TessResultRendererInsert(renderer, TessTextRendererCreate(outputbase));
                    }
                    break;
                case HOCR:
                    if (renderer == null) {
                        renderer = TessHOcrRendererCreate(outputbase);
                    } else {
                        TessResultRendererInsert(renderer, TessHOcrRendererCreate(outputbase));
                    }
                    break;
                case BOX:
                    if (renderer == null) {
                        renderer = TessBoxTextRendererCreate(outputbase);
                    } else {
                        TessResultRendererInsert(renderer, TessBoxTextRendererCreate(outputbase));
                    }
                    break;
                case UNLV:
                    if (renderer == null) {
                        renderer = TessUnlvRendererCreate(outputbase);
                    } else {
                        TessResultRendererInsert(renderer, TessUnlvRendererCreate(outputbase));
                    }
                    break;
            }
        }

        return renderer;
    }

    /**
     * Releases all of the native resources used by this instance.
     */
    protected void dispose() {
        TessBaseAPIDelete(handle);
    }

}

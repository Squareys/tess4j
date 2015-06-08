/**
 * Copyright @ 2008 Quan Nguyen
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
package net.sourceforge.tess4j.util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class ImageIOHelper {

    /**
     * Converts <code>BufferedImage</code> to <code>ByteBuffer</code>.
     *
     * @param bi Input image
     * @return pixel data
     */
    public static ByteBuffer convertImageData(BufferedImage bi) {
        DataBuffer buff = bi.getRaster().getDataBuffer();
        // ClassCastException thrown if buff not instanceof DataBufferByte because raster data is not necessarily bytes.
        // Convert the original buffered image to grayscale.
        if (!(buff instanceof DataBufferByte)) {
            bi = ImageHelper.convertImageToGrayscale(bi);
            buff = bi.getRaster().getDataBuffer();
        }
        byte[] pixelData = ((DataBufferByte) buff).getData();
        //        return ByteBuffer.wrap(pixelData);
        ByteBuffer buf = ByteBuffer.allocateDirect(pixelData.length);
        buf.order(ByteOrder.nativeOrder());
        buf.put(pixelData);
        buf.flip();
        return buf;
    }

    /**
     * Gets a list of <code>IIOImage</code> objects for a
     * <code>BufferedImage</code>.
     *
     * @param bi input image
     * @return a list of <code>IIOImage</code> objects
     * @throws IOException
     */
    public static List<RenderedImage> getImageList(BufferedImage bi) throws IOException {
        List<RenderedImage> iioImageList = new ArrayList<RenderedImage>();
        iioImageList.add(bi);
        return iioImageList;
    }
}

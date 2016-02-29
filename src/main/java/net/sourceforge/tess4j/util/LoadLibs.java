/**
 * Copyright @ 2014 Quan Nguyen
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

import java.util.logging.Logger;

import com.sun.jna.Native;
import com.sun.jna.Platform;

import net.sourceforge.tess4j.TessAPI;

/**
 * Loads native libraries from JAR or project folder.
 *
 * @author O.J. Sousa Rodrigues
 * @author Quan Nguyen
 */
public class LoadLibs {

    /**
     * Native library name.
     */
    public static String LIB_NAME = "libtesseract304";
    public static String LIB_NAME_NON_WIN = "tesseract";

    private final static Logger logger = Logger.getLogger(LoadLibs.class.getName());

    static {
        System.setProperty("jna.encoding", "UTF8");
    }

    /**
     * Loads Tesseract library via JNA.
     *
     * @return TessAPI instance being loaded using
     * <code>Native.loadLibrary()</code>.
     */
    public static TessAPI getTessAPIInstance() {
        return (TessAPI) Native.loadLibrary(getTesseractLibName(), TessAPI.class);
    }

    /**
     * Gets native library name.
     *
     * @return the name of the tesseract library to be loaded using the
     * <code>Native.register()</code>.
     */
    public static String getTesseractLibName() {
        return Platform.isWindows() ? LIB_NAME : LIB_NAME_NON_WIN;
    }
    
    /**
     * Set the tesseract windows library name.
     * @param name name of the library
     */
    public static void setTesseractLibraryName(String name) {
    	LIB_NAME = name;
    }
    
    /**
     * Set the tesseract non-windows library name.
     * @param name name of the library
     */
    public static void setTesseractLibraryNameNonWindows(String name) {
    	LIB_NAME = name;
    }
}

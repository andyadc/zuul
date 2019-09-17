package com.andyadc.zuul;

import java.io.File;

/**
 * Interface to generate Classes from source code
 */
public interface DynamicCodeCompiler {
    Class compile(String sCode, String sName) throws Exception;
    Class compile(File file) throws Exception;
}

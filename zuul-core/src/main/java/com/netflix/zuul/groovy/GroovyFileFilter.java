package com.netflix.zuul.groovy;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Filters only .groovy files
 */
public class GroovyFileFilter implements FilenameFilter {

    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith(".groovy");
    }

}
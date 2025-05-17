package me.z609.servers.server;

import java.io.File;
import java.io.FileFilter;

public class JarFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
        return !pathname.isDirectory() && pathname.getName().endsWith(".jar");
    }
}

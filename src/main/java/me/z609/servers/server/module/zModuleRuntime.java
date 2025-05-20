package me.z609.servers.server.module;

import me.z609.servers.server.zServer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.NoSuchAlgorithmException;

public class zModuleRuntime {

    private final zServer server;
    private final File jar;
    private URLClassLoader classLoader;

    private String md5;
    private zModule module;

    public zModuleRuntime(zServer server, File jar) throws MalformedURLException {
        this.server = server;
        this.jar = jar;

    }
    public zModuleRuntime(zServer server, File jar, URLClassLoader classLoader) {
        this.server = server;
        this.jar = jar;
        this.classLoader = classLoader;
    }

    public zModule load() throws IOException, NoSuchMethodException, ClassNotFoundException,
            InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchAlgorithmException {
        // Step 2: Create isolated ClassLoader
        this.md5 = zServer.md5(jar);

        if(classLoader == null){
            // Is a hot-swappable plugin, or no global context specified.
            this.classLoader = new URLClassLoader(new URL[]{
                    jar.toURI().toURL()
            }, server.getClassLoader());
        }

        // Step 3: Read module.yml inside jar
        zModuleDescription desc = zServer.retrieveModuleDescription(jar); // You must implement this

        // Step 4: Instantiate the module
        Class<?> clazz = Class.forName(desc.getMainClass(), true, classLoader);
        if (!zModule.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Invalid main class in " + jar.getName());
        }

        module = (zModule) clazz.getDeclaredConstructor().newInstance();
        module.hook(server, desc);

        return module;
    }

    public void unload() throws IOException {
        if (classLoader != null) {
            classLoader.close();
            classLoader = null;
        }
    }

    public zServer getServer() {
        return server;
    }

    public zModule getModule() {
        return module;
    }

    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    public File getJar() {
        return jar;
    }

    public String getMd5() {
        return md5;
    }
}

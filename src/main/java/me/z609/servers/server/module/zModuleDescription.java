package me.z609.servers.server.module;

import org.bukkit.configuration.Configuration;

import java.util.Collections;
import java.util.List;

public class zModuleDescription {

    private final Configuration config;

    private final String name;
    private final String version;
    private final String mainClass;
    private final List<String> depends;

    // Not required
    private final String description;
    private final String website;
    private final List<String> authors;
    private final boolean hotSwappable;

    public zModuleDescription(Configuration config) throws IllegalModuleDescriptionException {
        this.config = config;
        if(config == null)
            throw new IllegalModuleDescriptionException("Invalid module: No module.yml found or incompatible (is it up to date?)");
        if(!config.contains("name"))
            throw new IllegalModuleDescriptionException("Invalid module: Invalid `name` found in module.yml (is it up to date?)");
        if(!config.contains("version"))
            throw new IllegalModuleDescriptionException("Invalid module: Invalid `version` found in module.yml (is it up to date?)");
        if(!config.contains("main") || !config.isString("main"))
            throw new IllegalModuleDescriptionException("Invalid module: Invalid `main` class path found in module.yml (Is it up to date?)");

        this.name = config.getString("name");
        this.version = config.getString("version");
        this.mainClass = config.getString("main");
        this.depends = config.contains("depends") ? config.getStringList("depends") : Collections.emptyList();
        this.description = (config.contains("description") ? config.getString("description") : "");
        this.website = (config.contains("website") ? config.getString("website") : "");
        this.authors = (config.contains("authors") ? config.getStringList("authors") : Collections.emptyList());
        this.hotSwappable = (config.contains("hotSwappable") && config.getBoolean("hotSwappable"));
    }

    public Configuration getConfig() {
        return config;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getMainClass() {
        return mainClass;
    }

    public List<String> getDepends() {
        return depends;
    }

    public String getDescription() {
        return description;
    }

    public String getWebsite() {
        return website;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public boolean isHotSwappable() {
        return hotSwappable;
    }
}

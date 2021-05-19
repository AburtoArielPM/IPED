package dpf.sp.gpinf.indexer.config;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import iped3.configuration.IConfigurationDirectory;
import macee.core.Configurable;
import macee.core.ObjectManager;

public class ConfigurationManager implements ObjectManager<Configurable> {

    private static ConfigurationManager singleton = null;

    final HashMap<Object, Configurable> configurations = new HashMap<Object, Configurable>();

    IConfigurationDirectory directory;
    HashMap<Configurable, Boolean> loadedConfigurables = new LinkedHashMap<Configurable, Boolean>();

    public static ConfigurationManager getInstance() {
        return singleton;
    }

    public static ConfigurationManager createInstance(IConfigurationDirectory directory) {
        if (singleton == null) {
            synchronized (ConfigurationManager.class) {
                if (singleton == null) {
                    singleton = new ConfigurationManager(directory);
                }
            }
        }
        return singleton;
    }

    private ConfigurationManager(IConfigurationDirectory directory) {
        this.directory = directory;
    }

    public void addObject(Configurable config) {
        loadedConfigurables.put(config, false);
    }

    public void loadConfigs() throws IOException {
        for (Iterator<Configurable> iterator = loadedConfigurables.keySet().iterator(); iterator.hasNext();) {
            Configurable configurable = iterator.next();
            if (loadedConfigurables.get(configurable) == false) {
                List<Path> resources = directory.lookUpResource(configurable);

                configurable.processConfigs(resources);
                loadedConfigurables.put(configurable, new Boolean(true));
            }
        }
    }

    public void loadConfigs(boolean forceReload) throws IOException {
        for (Iterator<Configurable> iterator = loadedConfigurables.keySet().iterator(); iterator.hasNext();) {
            Configurable configurable = iterator.next();

            List<Path> resources = directory.lookUpResource(configurable);

            configurable.processConfigs(resources);
        }
    }

    public IConfigurationDirectory getConfigurationDirectory() {
        return directory;
    }

    @Override
    public Set<Configurable> findObjects(Class<? extends Configurable> clazz) {
        Set<Configurable> result = new HashSet<Configurable>();

        for (Iterator<Configurable> iterator = loadedConfigurables.keySet().iterator(); iterator.hasNext();) {
            Configurable configurable = iterator.next();
            if (configurable.getClass().equals(clazz)) {
                result.add(configurable);
            }
        }

        return result;
    }

    @Override
    public Set<Configurable> findObjects(String className) {
        Set<Configurable> result = new HashSet<Configurable>();

        for (Iterator<Configurable> iterator = loadedConfigurables.keySet().iterator(); iterator.hasNext();) {
            Configurable configurable = iterator.next();
            if (configurable.getClass().getName().equals(className)) {
                result.add(configurable);
            }
        }

        return result;
    }

    @Override
    public Set<Configurable> getObjects() {
        return loadedConfigurables.keySet();
    }

    @Override
    public void removeObject(Configurable aObject) {
        loadedConfigurables.remove(aObject);
    }

}
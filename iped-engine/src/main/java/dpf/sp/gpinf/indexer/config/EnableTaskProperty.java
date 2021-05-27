package dpf.sp.gpinf.indexer.config;

import java.io.IOException;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Path;

import macee.core.EnabledInterface;

public class EnableTaskProperty extends AbstractPropertiesConfigurable implements EnabledInterface {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String propertyName;
    private String value;

    public EnableTaskProperty(String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public Filter<Path> getResourceLookupFilter() {
        return new Filter<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                return entry.endsWith(IPEDConfig.CONFIG_FILE);
            }
        };
    }

    @Override
    public void processConfig(Path resource) throws IOException {
        properties.load(resource.toFile());
        String val = properties.getProperty(propertyName);
        if (val != null) {
            value = val.trim();
        }
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.valueOf(value);
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.value = String.valueOf(enabled);
    }

}

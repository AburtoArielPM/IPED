package dpf.sp.gpinf.indexer.config;

import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream.Filter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dpf.sp.gpinf.indexer.util.IPEDException;
import java.nio.file.Path;

public class NamedEntityTaskConfig extends AbstractPropertiesConfigurable {

    public static final String CONF_FILE = "NamedEntityRecognitionConfig.txt"; //$NON-NLS-1$

    private static final String ENABLE_PARAM = "enableNamedEntityRecogniton"; //$NON-NLS-1$

    private boolean taskEnabled;

    private String nerImpl;

    private float minLangScore = 0;

    private Map<String, String> langToModelMap = new HashMap<>();

    private Set<String> mimeTypesToIgnore = new HashSet<String>();

    private Set<String> categoriesToIgnore = new HashSet<String>();

    public boolean isTaskEnabled() {
        return taskEnabled;
    }

    public String getNerImpl() {
        return this.nerImpl;
    }

    public float getMinLangScore() {
        return minLangScore;
    }

    public Map<String, String> getLangToModelMap() {
        return langToModelMap;
    }

    public Set<String> getMimeTypesToIgnore() {
        return mimeTypesToIgnore;
    }

    public Set<String> getCategoriesToIgnore() {
        return categoriesToIgnore;
    }

    @Override
    public Filter<Path> getResourceLookupFilter() {
        return new Filter<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                return entry.endsWith(CONF_FILE) || entry.endsWith(IPEDConfig.CONFIG_FILE);
            }
        };
    }

    @Override
    public void processConfig(Path resource) throws IOException {

        properties.load(resource.toFile());

        if (resource.getFileName().equals(IPEDConfig.CONFIG_FILE)) {
            String value = properties.getProperty(ENABLE_PARAM);
            if (value != null) {
                taskEnabled = Boolean.valueOf(value.trim());
            }
        } else {

        }

        nerImpl = properties.getProperty("NERImpl"); //$NON-NLS-1$
        if (nerImpl.contains("CoreNLPNERecogniser")) { //$NON-NLS-1$
            try {
                Class.forName("edu.stanford.nlp.ie.crf.CRFClassifier"); //$NON-NLS-1$

            } catch (ClassNotFoundException e) {
                throw new IPEDException("StanfordCoreNLP not found. Did you put the jar in the optional lib folder?");
            }
        }

        String langAndModel;
        int i = 0;
        while ((langAndModel = properties.getProperty("langModel_" + i++)) != null) { //$NON-NLS-1$
            String[] strs = langAndModel.split(":"); //$NON-NLS-1$
            String lang = strs[0].trim();
            String modelPath = strs[1].trim();

            URL modelResource = this.getClass().getResource("/" + modelPath); //$NON-NLS-1$
            if (modelResource == null) {
                throw new IPEDException(modelPath + " not found. Did you put the model in the optional lib folder?");
            }

            langToModelMap.put(lang, modelPath);
        }

        String mimes = properties.getProperty("mimeTypesToIgnore"); //$NON-NLS-1$
        for (String mime : mimes.split(";")) { //$NON-NLS-1$
            mimeTypesToIgnore.add(mime.trim());
        }

        String categories = properties.getProperty("categoriesToIgnore"); //$NON-NLS-1$
        for (String cat : categories.split(";")) { //$NON-NLS-1$
            categoriesToIgnore.add(cat.trim());
        }

        minLangScore = Float.valueOf(properties.getProperty("minLangScore").trim()); //$NON-NLS-1$

    }

}

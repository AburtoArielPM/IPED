package dpf.sp.gpinf.indexer.process.task.transcript;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import dpf.sp.gpinf.indexer.config.AudioTranscriptConfig;
import dpf.sp.gpinf.indexer.config.ConfigurationManager;
import dpf.sp.gpinf.indexer.process.task.AbstractTask;
import iped3.IItem;
import macee.core.Configurable;

public class AudioTranscriptTask extends AbstractTask {

    private AbstractTranscriptTask impl;

    @Override
    public List<Configurable> getConfigurables() {
        return Arrays.asList(new AudioTranscriptConfig());
    }

    @Override
    public void init(Properties confParams, File confDir) throws Exception {
        AudioTranscriptConfig transcriptConfig = ConfigurationManager.findObject(AudioTranscriptConfig.class);

        impl = (AbstractTranscriptTask) Class.forName(transcriptConfig.getClassName()).newInstance();
        impl.setWorker(worker);
        impl.init(confParams, confDir);
    }

    public boolean isEnabled() {
        return impl.isEnabled();
    }

    @Override
    public void finish() throws Exception {
        impl.finish();
    }

    @Override
    protected void process(IItem evidence) throws Exception {
        impl.process(evidence);
    }

}

package dpf.sp.gpinf.indexer.process.task;

import java.io.File;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dpf.sp.gpinf.indexer.config.ConfigurationManager;
import dpf.sp.gpinf.indexer.config.LocalConfig;
import dpf.sp.gpinf.indexer.search.IPEDSearcher;
import dpf.sp.gpinf.indexer.search.IPEDSource;
import dpf.sp.gpinf.indexer.util.ImageUtil;
import iped3.IItem;
import jep.Jep;
import jep.JepException;
import jep.NDArray;
import jep.SharedInterpreter;

public class PythonTask extends AbstractTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(PythonTask.class);

    private static String scriptName;

    private File scriptFile;
    private Jep jep;
    private Properties confParams;
    private File confDir;
    private Boolean processQueueEnd;

    public PythonTask(File scriptFile) {
        this.scriptFile = scriptFile;
        try {
            loadScript();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loadScript() throws JepException {

        if (scriptName == null) {
            try (Jep jep = getNewJep(false)) {
                scriptName = (String) jep.invoke("getName");
            }
        }

    }

    private class ArrayConverter {
        public NDArray<?> getNDArray(byte[] array) {
            return new NDArray(array);
        }

        public NDArray<?> getNDArray(int[] array) {
            return new NDArray(array);
        }

        public NDArray<?> getNDArray(long[] array) {
            return new NDArray(array);
        }

        public NDArray<?> getNDArray(float[] array) {
            return new NDArray(array);
        }

        public NDArray<?> getNDArray(double[] array) {
            return new NDArray(array);
        }

        public NDArray<?> getNDArray(boolean[] array) {
            return new NDArray(array);
        }

        public NDArray<?> getNDArray(short[] array) {
            return new NDArray(array);
        }

        public NDArray<?> getNDArray(char[] array) {
            return new NDArray(array);
        }
    }

    private Jep getNewJepSync() throws JepException {
        synchronized (this.getClass()) {
            return getNewJep(true);
        }
    }

    private Jep getNewJep(boolean callInit) throws JepException {

        Jep jep = new SharedInterpreter();

        jep.eval("from jep import redirect_streams");
        jep.eval("redirect_streams.setup()");

        jep.set("caseData", this.caseData); //$NON-NLS-1$
        jep.set("moduleDir", this.output); //$NON-NLS-1$
        jep.set("worker", this.worker); //$NON-NLS-1$
        jep.set("stats", this.stats); //$NON-NLS-1$
        jep.set("logger", LOGGER); //$NON-NLS-1$
        jep.set("javaArray", new ArrayConverter()); //$NON-NLS-1$
        jep.set("javaTask", this); //$NON-NLS-1$
        jep.set("ImageUtil", new ImageUtil()); //$NON-NLS-1$

        LocalConfig localConfig = (LocalConfig) ConfigurationManager.getInstance().findObjects(LocalConfig.class)
                .iterator().next();
        jep.set("numThreads", Integer.valueOf(localConfig.getNumThreads()));

        jep.runScript(scriptFile.getAbsolutePath());

        if (callInit) {
            jep.invoke("init", confParams, confDir);
        }

        return jep;
    }

    @Override
    public String getName() {
        return scriptName; // $NON-NLS-1$
    }

    @Override
    public void init(Properties confParams, File confDir) throws Exception {
        this.confParams = confParams;
        this.confDir = confDir;
        try (Jep jep = getNewJep(true)) {
        }
    }

    @Override
    public void finish() throws Exception {

        try (IPEDSource ipedCase = new IPEDSource(this.output.getParentFile(), worker.writer)) {

            IPEDSearcher searcher = new IPEDSearcher(ipedCase);

            jep.set("ipedCase", ipedCase); //$NON-NLS-1$
            jep.set("searcher", searcher); //$NON-NLS-1$
            jep.invoke("finish"); //$NON-NLS-1$

        } finally {
            jep.close();
            jep = null;
        }

    }
    
    public void sendToNextTaskSuper(IItem item) throws Exception {
        super.sendToNextTask(item);
    }
    
    private boolean methodExists = true;
    
    @Override
    protected void sendToNextTask(IItem item) throws Exception {
        if(!methodExists) {
            super.sendToNextTask(item);
            return;
        }
        try {
            jep.invoke("sendToNextTask", item); //$NON-NLS-1$
            
        }catch(JepException e) {
            if(e.toString().contains("Unable to find object with name: sendToNextTask")) { //$NON-NLS-1$
                methodExists = false;
                super.sendToNextTask(item);
                return;
            }
            LOGGER.warn("Exception from " + scriptName + " on " + item.getPath() + ": " + e.toString());
            LOGGER.debug("", e);
        }
    }

    @Override
    protected boolean processQueueEnd() {
        if (processQueueEnd == null) {
            try {
                processQueueEnd = (Boolean) jep.invoke("processQueueEnd"); //$NON-NLS-1$
            } catch (JepException e) {
                processQueueEnd = false;
            }
        }
        return processQueueEnd;
    }

    @Override
    protected void process(IItem item) throws Exception {

        if (jep == null)
            jep = getNewJepSync();

        try {
            jep.invoke("process", item); //$NON-NLS-1$

        } catch (JepException e) {
            LOGGER.warn("Exception from " + scriptName + " on " + item.getPath() + ": " + e.toString(), e);
        }
    }

}

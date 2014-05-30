package org.jivesoftware.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.jivesoftware.openfire.provider.PropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilePropertiesProvider implements PropertiesProvider {
    private static final Logger log = LoggerFactory.getLogger(FilePropertiesProvider.class);

    private static final File PROPS_FILE = new File(System.getProperty("configFile"));
    private static final Properties PROPS = new Properties();

    public FilePropertiesProvider() {
        Reader reader = null;
        try {
            log.debug("Reading properties from file: {}", PROPS_FILE.getCanonicalPath());
            //System.out.println("### Reading properties from file: " + PROPS_FILE.getCanonicalPath());
            reader = new FileReader(PROPS_FILE);
            PROPS.load(reader);
        } catch (FileNotFoundException e) {
            log.error("Could not find properties file to read {}", PROPS_FILE.getAbsolutePath());
        } catch (IOException e) {
            log.error("Could not read properties file {} - reason {}", PROPS_FILE.getAbsolutePath(), e.getMessage());
        } finally {
            if (reader != null) {
                closeStream(reader);
            }
        }
}

    /**
     * {@inheritDoc}
     */
    public Map<String, String> loadProperties() {
        Map<String, String> asMap = new HashMap<String, String>();

        for(Entry<Object,Object> entry: PROPS.entrySet()){
            asMap.put((String)entry.getKey(), ((String)entry.getValue()).trim());
        }

        return asMap;
    }

    /**
     * {@inheritDoc}
     */
    public void insertProperty(String name, String value) {
        synchronized (PROPS) {
            PROPS.put(name, value);

            save();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void updateProperty(String name, String value) {
        insertProperty(name, value);
    }

    /**
     * {@inheritDoc}
     */
    public void deleteProperty(String name) {
        synchronized (PROPS) {
            PROPS.remove(name);

            save();
}
    }

    private static void save() {
        Writer writer = null;
        try {
            writer = new FileWriter(PROPS_FILE);
            PROPS.store(writer, "");
        } catch (FileNotFoundException e) {
            log.error("Could not find properties file to write {}", PROPS_FILE.getAbsolutePath());
        } catch (IOException e) {
            log.error("Could not write properties file {} - reason {}", PROPS_FILE.getAbsolutePath(), e.getMessage());
        } finally {
            closeStream((Closeable) writer);
        }
    }

    private static void closeStream(Closeable stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (final IOException ioe) {
            // ignore
        }
    }

}
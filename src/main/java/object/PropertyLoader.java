package object;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyLoader {
    private static volatile PropertyLoader instance;
    private static String propFileName;
    private static Logger logger = Logger.getLogger(PropertyLoader.class);

    private PropertyLoader() {
    }

    public static PropertyLoader initialize() {
        if (null == instance) {
            synchronized (PropertyLoader.class) {
                if (null == instance) {
                    instance = new PropertyLoader();
                    propFileName = System.getProperty("test.property.file");
                    logger.info("[Property File] : " + propFileName);

                    if (null == propFileName)
                        propFileName = "resource.properties";
                }
            }
        }
        return instance;
    }

    public String getProperty(String key) throws IOException {
        Properties prop = new Properties();
        String value = "";

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName)) {
            if (inputStream == null) return value;

            prop.load(inputStream);
            value = prop.getProperty(key);
        }

        return value;
    }
}

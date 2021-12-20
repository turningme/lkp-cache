package org.lkpnotice.infra.headlessbrowser;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class LogicLoader {
    private static final Logger LOGGER = Logger.getLogger(LogicLoader.class);
    private String logic;

    /**
     * Reads the file that contains all of the extraction logic and libraries
     * needed into a string for use by a JavascriptExecutor
     */
    public LogicLoader(String logicFilePath) {
        try {
            logic = FileUtils.readFileToString(new File(logicFilePath));
        } catch (IOException e) {
            LOGGER.error("Could not read file containing extraction logic: " + e);
            throw new RuntimeException("Could not read logic to perform extraction");
        }
    }

    /**
     * Returns a string with all of the logic needed to extract content
     * from a web page.
     * @requires logicFilePath has been set
     */
    public String getLogic() {
        return logic;
    }
}

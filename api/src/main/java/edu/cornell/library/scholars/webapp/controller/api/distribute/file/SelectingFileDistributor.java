/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.library.scholars.webapp.controller.api.distribute.AbstractDataDistributor;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext;
import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

/**
 * <p>
 * Selects one of several files and distributes it, based on the value of a
 * request parameter. If the selected file does not exist, distributes an empty
 * data set.
 * </p>
 * <p>
 * The configuration includes:
 * </p>
 * <ul>
 * <li>The name of the request parameter that will contain the file
 * selector.</li>
 * <li>A regular expression that will be used to extract the file selector from
 * the parameter value.</li>
 * <li>A template for the file path.</li>
 * <li>A string to be served as an "empty data set".</li>
 * <li>The content type to attach to the output.</li>
 * </ul>
 *
 * <p>
 * An example: this configuration will distribute JSON files based on the
 * localname of the department URI, as specified in the request.
 * </p>
 * <pre>
 * :sfd
 *   a   &lt;java:edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor&gt; ,
 *       &lt;java:edu.cornell.library.scholars.webapp.controller.api.distribute.file.SelectingFileDistributor&gt; ;
 *   :actionName "collaboration_sunburst" ;
 *   :contentType "application/json" ;
 *   :parameterName "department" ;
 *   :parameterPattern "[^/#]+$";
 *   :filepathTemplate "crossunit-\\0.json" ;
 *   :emptyResponse "[]" .
 * </pre>
 * 
 * <p>
 * When the request is received, the "department" parameter contains the URI of
 * the department in question. The provided pattern matcher will get just the
 * localname from the department URI. That localname is substituted into the
 * path template to determine the location of the file. If a file exists at that
 * location, it's contents are served as "application/json" text. If the file
 * does not exist, the empty response (an empty JSON array) is served.
 * </p>
 */
public class SelectingFileDistributor extends AbstractDataDistributor {

    private static final Log log = LogFactory
            .getLog(SelectingFileDistributor.class);

    /** The name of the request parameter that will select the file. */
    private String parameterName;

    /** The pattern to parse the value of the request parameter. */
    private Pattern parameterParser;

    /** The template to create the file path from the parsed values. */
    private String filepathTemplate;

    /** The content type to attach to the file. */
    private String contentType;

    /** The response to provide if the file does not exist. */
    private String emptyResponse;

    private FileFinder fileFinder;

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#parameterName", minOccurs = 1, maxOccurs = 1)
    public void setParameterName(String name) {
        parameterName = name;
    }

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#parameterPattern", minOccurs = 1, maxOccurs = 1)
    public void setParameterPattern(String pattern) {
        parameterParser = Pattern.compile(pattern);
    }

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#filepathTemplate", minOccurs = 1, maxOccurs = 1)
    public void setFilepathTemplate(String template) {
        filepathTemplate = template;
    }

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#contentType", minOccurs = 1, maxOccurs = 1)
    public void setContentType(String cType) {
        contentType = cType;
    }

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#emptyResponse", minOccurs = 1, maxOccurs = 1)
    public void setEmptyResponse(String response) {
        emptyResponse = response;
    }

    @Override
    public void init(DataDistributorContext context)
            throws DataDistributorException {
        super.init(context);
        fileFinder = new FileFinder(parameters, parameterName, parameterParser,
                filepathTemplate,
                ApplicationUtils.instance().getHomeDirectory().getPath());
    }

    @Override
    public String getContentType() throws DataDistributorException {
        return contentType;
    }

    @Override
    public void writeOutput(OutputStream output)
            throws DataDistributorException {
        try {
            File file = fileFinder.find();
            if (file != null && file.isFile()) {
                IOUtils.copy(new FileInputStream(file), output);
                return;
            } else {
                IOUtils.write(emptyResponse, output, "UTF-8");
            }
        } catch (IOException e) {
            throw new DataDistributorException(e);
        }
    }

    @Override
    public void close() throws DataDistributorException {
        // Nothing to close.
    }

    /**
     * Does the heavy lifting of locating the file.
     */
    protected static class FileFinder {
        private final Map<String, String[]> parameters;
        private final String parameterName;
        private final Pattern parameterParser;
        private final String filepathTemplate;
        private final Path home;

        public FileFinder(Map<String, String[]> parameters,
                String parameterName, Pattern parameterParser,
                String filepathTemplate, Path home) {
            this.parameters = parameters;
            this.parameterName = parameterName;
            this.parameterParser = parameterParser;
            this.filepathTemplate = filepathTemplate;
            this.home = home;
        }

        public File find() {
            String parameter = getParameterFromRequest();
            if (parameter == null) {
                return null;
            } else {
                return doPatternMatching(parameter);
            }
        }

        private String getParameterFromRequest() {
            String[] values = parameters.get(parameterName);
            if (log.isDebugEnabled()) {
                log.debug("Parameter value: =" + Arrays.asList(values));
            }
            if (values == null || values.length == 0) {
                log.warn("No value provided for request parameter '"
                        + parameterName + "'");
                return null;
            }
            if (values.length > 1) {
                log.warn("Multiple values provided for request parameter '"
                        + parameterName + "': " + Arrays.deepToString(values));
                return null;
            }
            return values[0];
        }

        private File doPatternMatching(String parameter) {
            Matcher m = parameterParser.matcher(parameter);
            log.debug("Pattern matching: value=" + parameter + ", parser="
                    + parameterParser + ", match=" + m);
            if (m.find()) {
                return substituteIntoFilepath(m);
            } else {
                log.warn("Failed to parse the request parameter: '"
                        + parameterParser + "' doesn't match '" + parameter
                        + "'");
                return null;
            }

        }

        private File substituteIntoFilepath(Matcher m) {
            String path = filepathTemplate;
            for (int i = 0; i <= m.groupCount(); i++) {
                path = path.replace("\\" + i, m.group(i));
            }
            log.debug("Substitute: " + filepathTemplate + " becomes " + path);
            return home.resolve(path).toFile();
        }

    }
}

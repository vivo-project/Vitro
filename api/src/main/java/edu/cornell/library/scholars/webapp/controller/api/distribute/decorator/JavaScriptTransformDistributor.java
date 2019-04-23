/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.decorator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.library.scholars.webapp.controller.api.distribute.AbstractDataDistributor;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext;
import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

/**
 * Wrap a data distributor with a JavaScript function that will transform its
 * output.
 * 
 * <p>
 * 
 * The child distributor might produce any arbitrary output. The JavaScript
 * function must be written to accept that output as a String and return a
 * String containing the transformed output.
 * 
 * <p>
 * 
 * For example, the function might replace all occurences of a namespace with a
 * different namespace, like this:
 * 
 * <pre>
 * function transform(rawData) {
 *     return rawData.split("http://first/").join("http://second/");
 * }
 * </pre>
 * 
 * The JavaScript method must be named 'transform', must accept a String as
 * argument, and must return a String as result.
 * 
 * <p>
 * 
 * The JavaScript execution environment will include a global variable named
 * 'logger'. This is a binding of an org.apache.commons.logging.Log object, and
 * can be used to write to the VIVO log file.
 * 
 * <p>
 * 
 * Note: this decorator is only scalable to a limited extent, since the
 * JavaScript function works with Strings instead of Streams.
 */
public class JavaScriptTransformDistributor extends AbstractDataDistributor {
    private static final Log log = LogFactory
            .getLog(JavaScriptTransformDistributor.class);

    /** The content type to attach to the file. */
    private String contentType;
    private String script;
    private DataDistributor child;
    private List<String> supportingScriptPaths = new ArrayList<>();

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#contentType", minOccurs = 1, maxOccurs = 1)
    public void setContentType(String cType) {
        contentType = cType;
    }

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#script", minOccurs = 1, maxOccurs = 1)
    public void setScript(String scriptIn) {
        script = scriptIn;
    }

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#child", minOccurs = 1, maxOccurs = 1)
    public void setChild(DataDistributor c) {
        child = c;
    }

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#supportingScript")
    public void addScriptPath(String path) {
        supportingScriptPaths.add(path);
    }

    @Override
    public String getContentType() throws DataDistributorException {
        return contentType;
    }

    @Override
    public void init(DataDistributorContext ddc)
            throws DataDistributorException {
        super.init(ddc);
        child.init(ddc);
    }

    /**
     */
    @Override
    public void writeOutput(OutputStream output)
            throws DataDistributorException {
        ScriptEngine engine = createScriptEngine();
        addLoggerToEngine(engine);
        loadSupportingScripts(engine);
        loadMainScript(engine);

        writeTransformedOutput(output,
                runTransformFunction(engine, runChildDistributor()));
    }

    private ScriptEngine createScriptEngine() {
        return new ScriptEngineManager().getEngineByName("nashorn");
    }

    private void addLoggerToEngine(ScriptEngine engine) {
        String loggerName = this.getClass().getName() + "." + actionName;
        Log jsLogger = LogFactory.getLog(loggerName);
        engine.put("logger", jsLogger);
    }

    private void loadSupportingScripts(ScriptEngine engine)
            throws DataDistributorException {
        log.debug("loading supporting scripts");
        for (String path : supportingScriptPaths) {
            loadSupportingScript(engine, path);
            log.debug("loaded supporting script: " + path);
        }
    }

    private void loadSupportingScript(ScriptEngine engine, String path)
            throws DataDistributorException {
        ServletContext ctx = ApplicationUtils.instance().getServletContext();

        InputStream resource = ctx.getResourceAsStream(path);
        if (resource == null) {
            throw new DataDistributorException(
                    "Can't locate script resource for '" + path + "'");
        }

        try {
            engine.eval(new InputStreamReader(resource));
        } catch (ScriptException e) {
            throw new DataDistributorException(
                    "Script at '" + path + "' contains syntax errors.", e);
        }
    }

    private void loadMainScript(ScriptEngine engine)
            throws DataDistributorException {
        try {
            engine.eval(script);
        } catch (ScriptException e) {
            throw new DataDistributorException("Script contains syntax errors.",
                    e);
        }
    }

    private String runChildDistributor() throws DataDistributorException {
        ByteArrayOutputStream childOut = new ByteArrayOutputStream();
        try {
            child.writeOutput(childOut);
            log.debug("ran child distributor");
        } catch (Exception e) {
            throw new DataDistributorException(
                    "Child distributor threw an exception", e);
        }
        try {
            return childOut.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("What? No UTF-8 Charset?", e);
        }
    }

    private String runTransformFunction(ScriptEngine engine, String childOutput)
            throws DataDistributorException {
        try {
            Invocable invocable = (Invocable) engine;
            Object result = invocable.invokeFunction("transform", childOutput);
            log.debug("ran transform function");

            if (result instanceof String) {
                return (String) result;
            } else {
                throw new ActionFailedException(
                        "transform function must return a String");
            }
        } catch (NoSuchMethodException e) {
            throw new DataDistributorException(
                    "Script must have a transform() function.", e);
        } catch (ScriptException e) {
            throw new DataDistributorException("Script contains syntax errors.",
                    e);
        }
    }

    private void writeTransformedOutput(OutputStream output, String transformed)
            throws DataDistributorException {
        try {
            output.write(transformed.getBytes("UTF-8"));
        } catch (IOException e) {
            throw new DataDistributorException(e);
        }
    }

    @Override
    public void close() throws DataDistributorException {
        child.close();
    }

}

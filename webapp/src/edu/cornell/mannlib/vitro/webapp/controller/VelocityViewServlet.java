/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Properties;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.io.VelocityWriter;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.tools.generic.log.LogSystemCommonsLog;
import org.apache.velocity.tools.view.ToolboxManager;
import org.apache.velocity.tools.view.context.ChainedContext;
import org.apache.velocity.util.SimplePool;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

/**
 * RY Copying VelocityViewServlet into our code base in order to debug and
 * modify the source.
 * 
 * <p>A servlet to process Velocity templates. This is comparable to the
 * the JspServlet for JSP-based applications.</p>
 *
 * <p>The servlet provides the following features:</p>
 * <ul>
 *   <li>renders Velocity templates</li>
 *   <li>provides support for an auto-loaded, configurable toolbox</li>
 *   <li>provides transparent access to the servlet request attributes,
 *       servlet session attributes and servlet context attributes by
 *       auto-searching them</li>
 *   <li>logs to the logging facility of the servlet API</li>
 * </ul>
 *
 * <p>VelocityViewServlet supports the following configuration parameters
 * in web.xml:</p>
 * <dl>
 *   <dt>org.apache.velocity.toolbox</dt>
 *   <dd>Path and name of the toolbox configuration file. The path must be
 *     relative to the web application root directory. If this parameter is
 *     not found, the servlet will check for a toolbox file at
 *     '/WEB-INF/toolbox.xml'.</dd>
 *   <dt>org.apache.velocity.properties</dt>
 *   <dd>Path and name of the Velocity configuration file. The path must be
 *     relative to the web application root directory. If this parameter
 *     is not present, Velocity will check for a properties file at
 *     '/WEB-INF/velocity.properties'.  If no file is found there, then
 *     Velocity is initialized with the settings in the classpath at
 *     'org.apache.velocity.tools.view.servlet.velocity.properties'.</dd>
 * </dl>
 *
 * <p>There are methods you may wish to override to access, alter or control
 * any part of the request processing chain.  Please see the javadocs for
 * more information on :
 * <ul>
 * <li> {@link #loadConfiguration} : <br>for loading Velocity properties and
 *                                     configuring the Velocity runtime
 * <li> {@link #setContentType} : <br>for changing the content type on a request
 *                                  by request basis
 * <li> {@link #fillContext} : <br>for add data to the context before
 *                               template rendering
 * <li> {@link #requestCleanup} : <br>post rendering resource or other cleanup
 * <li> {@link #error} : <br>error handling
 * </ul>
 * </p>
 *
 * @author Dave Bryson
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:kjohnson@transparent.com">Kent Johnson</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author Nathan Bubna
 *
 * @version $Id: VelocityViewServlet.java 595092 2007-11-14 22:15:13Z nbubna $
 */

public class VelocityViewServlet extends HttpServlet
{

    /** serial version id */
    private static final long serialVersionUID = -3329444102562079189L;

    /** The HTTP content type context key. */
    public static final String CONTENT_TYPE = "default.contentType";

    /** The default content type for the response */
    public static final String DEFAULT_CONTENT_TYPE = "text/html";

    /** Default encoding for the output stream */
    public static final String DEFAULT_OUTPUT_ENCODING = "ISO-8859-1";

    /**
     * Key used to access the ServletContext in
     * the Velocity application attributes.
     */
    public static final String SERVLET_CONTEXT_KEY =
        ServletContext.class.getName();

    /**
     * Default Runtime properties.
     */
    public static final String DEFAULT_TOOLS_PROPERTIES =
        "/org/apache/velocity/tools/view/servlet/velocity.properties";


    /**
     * Key used to access the toolbox configuration file path from the
     * Servlet or webapp init parameters ("org.apache.velocity.toolbox").
     */
    protected static final String TOOLBOX_KEY =
        "org.apache.velocity.toolbox";

    /**
     * This is the string that is looked for when getInitParameter is
     * called ("org.apache.velocity.properties").
     */
    protected static final String INIT_PROPS_KEY =
        "org.apache.velocity.properties";

    /**
     * Default toolbox configuration file path. If no alternate value for
     * this is specified, the servlet will look here.
     */
    protected static final String DEFAULT_TOOLBOX_PATH =
        "/WEB-INF/velocity/toolbox.xml";

    /**
     * Default velocity properties file path. If no alternate value for
     * this is specified, the servlet will look here.
     */
    protected static final String DEFAULT_PROPERTIES_PATH =
        "/WEB-INF/velocity/velocity.properties";

    /** A reference to the toolbox manager. */
    protected ToolboxManager toolboxManager = null;
    
    /**
     * Vitro fields
     */
	protected Portal portal;

    /** Cache of writers */
    private static SimplePool writerPool = new SimplePool(40);

    /* The engine used to process templates. */
    private VelocityEngine velocity = null;

    /**
     * The default content type.  When necessary, includes the
     * character set to use when encoding textual output.
     */
    private String defaultContentType;

    /**
     * Whether we've logged a deprecation warning for
     * ServletResponse's <code>getOutputStream()</code>.
     * @since VelocityTools 1.1
     */
    private boolean warnOfOutputStreamDeprecation = true;



    /**
     * <p>Initializes servlet, toolbox and Velocity template engine.
     * Called by the servlet container on loading.</p>
     *
     * <p>NOTE: If no charset is specified in the default.contentType
     * property (in your velocity.properties) and you have specified
     * an output.encoding property, then that will be used as the
     * charset for the default content-type of pages served by this
     * servlet.</p>
     *
     * @param config servlet configuation
     */
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        // do whatever we have to do to init Velocity
        initVelocity(config);

        // init this servlet's toolbox (if any)
        initToolbox(config);

        // we can get these now that velocity is initialized
        defaultContentType =
            (String)getVelocityProperty(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);

        String encoding =
            (String)getVelocityProperty(RuntimeConstants.OUTPUT_ENCODING,
                                        DEFAULT_OUTPUT_ENCODING);

        // For non Latin-1 encodings, ensure that the charset is
        // included in the Content-Type header.
        if (!DEFAULT_OUTPUT_ENCODING.equalsIgnoreCase(encoding))
        {
            int index = defaultContentType.lastIndexOf("charset");
            if (index < 0)
            {
                // the charset specifier is not yet present in header.
                // append character encoding to default content-type
                defaultContentType += "; charset=" + encoding;
            }
            else
            {
                // The user may have configuration issues.
                velocity.warn("VelocityViewServlet: Charset was already " +
                              "specified in the Content-Type property.  " +
                              "Output encoding property will be ignored.");
            }
        }

        velocity.info("VelocityViewServlet: Default content-type is: " +
                      defaultContentType);
    }


    /**
     * Looks up an init parameter with the specified key in either the
     * ServletConfig or, failing that, in the ServletContext.
     */
    protected String findInitParameter(ServletConfig config, String key)
    {
        // check the servlet config
        String param = config.getInitParameter(key);

        if (param == null || param.length() == 0)
        {
            // check the servlet context
            ServletContext servletContext = config.getServletContext();
            param = servletContext.getInitParameter(key);
        }
        return param;
    }


    /**
     * Simplifies process of getting a property from VelocityEngine,
     * because the VelocityEngine interface sucks compared to the singleton's.
     * Use of this method assumes that {@link #initVelocity(ServletConfig)}
     * has already been called.
     */
    protected String getVelocityProperty(String key, String alternate)
    {
        String prop = (String)velocity.getProperty(key);
        if (prop == null || prop.length() == 0)
        {
            return alternate;
        }
        return prop;
    }


    /**
     * Returns the underlying VelocityEngine being used.
     */
    protected VelocityEngine getVelocityEngine()
    {
        return velocity;
    }

    /**
     * Sets the underlying VelocityEngine
     */
    protected void setVelocityEngine(VelocityEngine ve)
    {
        if (ve == null)
        {
            throw new NullPointerException("Cannot set the VelocityEngine to null");
        }
        this.velocity = ve;
    }


    /**
     * Initializes the ServletToolboxManager for this servlet's
     * toolbox (if any).
     *
     * @param config servlet configuation
     */
    protected void initToolbox(ServletConfig config) throws ServletException
    {
        /* check the servlet config and context for a toolbox param */
        String file = findInitParameter(config, TOOLBOX_KEY);
        if (file == null)
        {
            // ok, look in the default location
            file = DEFAULT_TOOLBOX_PATH;
            velocity.debug("VelocityViewServlet: No toolbox entry in configuration."
                           + " Looking for '" + DEFAULT_TOOLBOX_PATH + "'");
        }

        /* try to get a manager for this toolbox file */
        toolboxManager =
        	org.apache.velocity.tools.view.servlet.ServletToolboxManager.getInstance(getServletContext(), file);
    }


    /**
     * Initializes the Velocity runtime, first calling
     * loadConfiguration(ServletConfig) to get a
     * org.apache.commons.collections.ExtendedProperties
     * of configuration information
     * and then calling velocityEngine.init().  Override this
     * to do anything to the environment before the
     * initialization of the singleton takes place, or to
     * initialize the singleton in other ways.
     *
     * @param config servlet configuration parameters
     */
    protected void initVelocity(ServletConfig config) throws ServletException
    {
        velocity = new VelocityEngine();
        setVelocityEngine(velocity);

        // register this engine to be the default handler of log messages
        // if the user points commons-logging to the LogSystemCommonsLog
        LogSystemCommonsLog.setVelocityEngine(velocity);

        velocity.setApplicationAttribute(SERVLET_CONTEXT_KEY, getServletContext());

        // Try reading the VelocityTools default configuration
        try
        {
            ExtendedProperties defaultProperties = loadDefaultProperties();
            velocity.setExtendedProperties(defaultProperties);
        }
        catch(Exception e)
        {
            log("VelocityViewServlet: Unable to read Velocity Servlet configuration file: ", e);

            // This is a fatal error...
            throw new ServletException(e);
        }

        // Try reading an overriding user Velocity configuration
        try
        {
            ExtendedProperties p = loadConfiguration(config);
            velocity.setExtendedProperties(p);
        }
        catch(Exception e)
        {
            log("VelocityViewServlet: Unable to read Velocity configuration file: ", e);
            log("VelocityViewServlet: Using default Velocity configuration.");
        }

        // now all is ready - init Velocity
        try
        {
            velocity.init();
        }
        catch(Exception e)
        {
            log("VelocityViewServlet: PANIC! unable to init()", e);
            throw new ServletException(e);
        }
    }

    private ExtendedProperties loadDefaultProperties()
    {
        InputStream inputStream = null;
        ExtendedProperties defaultProperties = new ExtendedProperties();

        try
        {
            inputStream = getClass()
                    .getResourceAsStream(DEFAULT_TOOLS_PROPERTIES);
            if (inputStream != null)
            {
                defaultProperties.load(inputStream);
            }
        }
        catch (IOException ioe)
        {
            log("Cannot load default extendedProperties!", ioe);
        }
        finally
        {
            try
            {
                if (inputStream != null)
                {
                    inputStream.close();
                }
            }
            catch (IOException ioe)
            {
                log("Cannot close default extendedProperties!", ioe);
            }
        }
        return defaultProperties;
    }


    /**
     *  Loads the configuration information and returns that
     *  information as an ExtendedProperties, which will be used to
     *  initialize the Velocity runtime.
     *  <br><br>
     *  Currently, this method gets the initialization parameter
     *  VelocityServlet.INIT_PROPS_KEY, which should be a file containing
     *  the configuration information.
     *  <br><br>
     *  To configure your Servlet Spec 2.2 compliant servlet runner to pass
     *  this to you, put the following in your WEB-INF/web.xml file
     *  <br>
     *  <pre>
     *    &lt;servlet&gt;
     *      &lt;servlet-name&gt; YourServlet &lt/servlet-name&gt;
     *      &lt;servlet-class&gt; your.package.YourServlet &lt;/servlet-class&gt;
     *      &lt;init-param&gt;
     *         &lt;param-name&gt; org.apache.velocity.properties &lt;/param-name&gt;
     *         &lt;param-value&gt; velocity.properties &lt;/param-value&gt;
     *      &lt;/init-param&gt;
     *    &lt;/servlet&gt;
     *   </pre>
     *
     * Alternately, if you wish to configure an entire context in this
     * fashion, you may use the following:
     *  <br>
     *  <pre>
     *    &lt;context-param&gt;
     *       &lt;param-name&gt; org.apache.velocity.properties &lt;/param-name&gt;
     *       &lt;param-value&gt; velocity.properties &lt;/param-value&gt;
     *       &lt;description&gt; Path to Velocity configuration &lt;/description&gt;
     *    &lt;/context-param&gt;
     *   </pre>
     *
     *  Derived classes may do the same, or take advantage of this code to do the loading for them via :
     *   <pre>
     *      ExtendedProperties p = super.loadConfiguration(config);
     *   </pre>
     *  and then add or modify the configuration values from the file.
     *  <br>
     *
     *  @param config ServletConfig passed to the servlets init() function
     *                Can be used to access the real path via ServletContext (hint)
     *  @return ExtendedProperties loaded with configuration values to be used
     *          to initialize the Velocity runtime.
     *  @throws IOException I/O problem accessing the specified file, if specified.
     */
    protected ExtendedProperties loadConfiguration(ServletConfig config)
        throws IOException
    {
        // grab the path to the custom props file (if any)
        String propsFile = findInitParameter(config, INIT_PROPS_KEY);
        if (propsFile == null)
        {
            // ok, look in the default location for custom props
            propsFile = DEFAULT_PROPERTIES_PATH;
            velocity.debug("VelocityViewServlet: Looking for custom properties at '"
                           + DEFAULT_PROPERTIES_PATH + "'");
        }

        ExtendedProperties p = new ExtendedProperties();
        InputStream is = getServletContext().getResourceAsStream(propsFile);
        if (is != null)
        {
            // load the properties from the input stream
            p.load(is);
            velocity.info("VelocityViewServlet: Using custom properties at '"
                          + propsFile + "'");
        }
        else
        {
            velocity.debug("VelocityViewServlet: No custom properties found. " +
                           "Using default Velocity configuration.");
        }
        return p;
    }


    /**
     * Handles GET - calls doRequest()
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        doRequest(request, response);
    }


    /**
     * Handle a POST request - calls doRequest()
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        doRequest(request, response);
    }


    /**
     *  Handles with both GET and POST requests
     *
     *  @param request  HttpServletRequest object containing client request
     *  @param response HttpServletResponse object for the response
     */
    protected void doRequest(HttpServletRequest request,
                             HttpServletResponse response)
         throws ServletException, IOException
    {
    	System.out.println("IN VELOCITY VIEW SERVLET");
        Context context = null;
        try
        {
            // first, get a context
            context = createContext(request, response);

            fillContext(context, request);

            // set the content type
            setContentType(request, response);

            // get the template
            Template template = handleRequest(request, response, context);

            // bail if we can't find the template
            if (template == null)
            {
                velocity.warn("VelocityViewServlet: couldn't find template to match request.");
                return;
            }

            // merge the template and context
            mergeTemplate(template, context, response);
        }
        catch (Exception e)
        {
            // log the exception
            velocity.error("VelocityViewServlet: Exception processing the template: "+e);

            // call the error handler to let the derived class
            // do something useful with this failure.
            error(request, response, e);
        }
        finally
        {
            // call cleanup routine to let a derived class do some cleanup
            requestCleanup(request, response, context);
        }
    }


    /**
     * <p>This was a common extension point, but has been deprecated.
     * It has no single replacement.  Instead, you should override 
     * {@link #fillContext} to add custom things to the {@link Context}
     * or override a {@link #getTemplate} method to change how
     * {@link Template}s are retrieved</p>
     *
     * @param request client request
     * @param response client response
     * @param ctx  VelocityContext to fill
     * @deprecated This will be removed in VelocityTools 2.0.
     * @return Velocity Template object or null
     */
    protected Template handleRequest(HttpServletRequest request,
                                     HttpServletResponse response,
                                     Context ctx)
        throws Exception
    {
        return getTemplate(request, response);
    }


    /**
     * <p>Creates and returns an initialized Velocity context.</p>
     *
     * A new context of class {@link ChainedContext} is created and
     * initialized.
     *
     * @param request servlet request from client
     * @param response servlet reponse to client
     */
    protected Context createContext(HttpServletRequest request,
                                    HttpServletResponse response)
    {
        ChainedContext ctx =
            new ChainedContext(velocity, request, response, getServletContext());

        /* if we have a toolbox manager, get a toolbox from it */
        if (toolboxManager != null)
        {
            ctx.setToolbox(toolboxManager.getToolbox(ctx));
        }
        return ctx;
    }


    /**
     * This is an extension hook for users who subclass this servlet to
     * make their own modifications to the {@link Context}. It is a partial
     * replacement of the deprecated {@link #handleRequest} method. This
     * implementation does nothing.
     */
    protected void fillContext(Context context, HttpServletRequest request)
    {
        // this implementation does nothing
    }
        

    /**
     * Sets the content type of the response.  This is available to be overriden
     * by a derived class.
     *
     * <p>The default implementation is :
     * <pre>
     *    response.setContentType(defaultContentType);
     * </pre>
     * where defaultContentType is set to the value of the default.contentType
     * property, or "text/html" if that is not set.</p>
     *
     * @param request servlet request from client
     * @param response servlet reponse to client
     */
    protected void setContentType(HttpServletRequest request,
                                  HttpServletResponse response)
    {
        response.setContentType(defaultContentType);
    }


    /**
     * <p>Gets the requested template.</p>
     *
     * @param request client request
     * @return Velocity Template object or null
     */
    protected Template getTemplate(HttpServletRequest request)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        return getTemplate(request, null);
    }

    /**
     * <p>Gets the requested template.</p>
     *
     * @param request client request
     * @param response client response (whose character encoding we'll use)
     * @return Velocity Template object or null
     */
    protected Template getTemplate(HttpServletRequest request,
                                   HttpServletResponse response)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        String path = org.apache.velocity.tools.view.servlet.ServletUtils.getPath(request);
        if (response == null)
        {
            return getTemplate(path);
        }
        else
        {
            return getTemplate(path, response.getCharacterEncoding());
        }
    }


    /**
     * Retrieves the requested template.
     *
     * @param name The file name of the template to retrieve relative to the
     *             template root.
     * @return The requested template.
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     * @throws Exception if an error occurs in template initialization
     */
    public Template getTemplate(String name)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        return getTemplate(name, null);
    }


    /**
     * Retrieves the requested template with the specified character encoding.
     *
     * @param name The file name of the template to retrieve relative to the
     *             template root.
     * @param encoding the character encoding of the template
     * @return The requested template.
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     * @throws Exception if an error occurs in template initialization
     */
    public Template getTemplate(String name, String encoding)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        if (encoding == null)
        {
            return getVelocityEngine().getTemplate(name);
        }
        else
        {
            return getVelocityEngine().getTemplate(name, encoding);
        }
    }


    /**
     * Merges the template with the context.  Only override this if you really, really
     * really need to. (And don't call us with questions if it breaks :)
     *
     * @param template template object returned by the handleRequest() method
     * @param context Context created by the {@link #createContext}
     * @param response servlet reponse (used to get a Writer)
     */
    protected void mergeTemplate(Template template,
                                 Context context,
                                 HttpServletResponse response)
        throws ResourceNotFoundException, ParseErrorException,
               MethodInvocationException, IOException,
               UnsupportedEncodingException, Exception
    {
        VelocityWriter vw = null;
        Writer writer = getResponseWriter(response);
        try
        {
            vw = (VelocityWriter)writerPool.get();
            if (vw == null)
            {
                vw = new VelocityWriter(writer, 4 * 1024, true);
            }
            else
            {
                vw.recycle(writer);
            }
            performMerge(template, context, vw);
        }
        finally
        {
            if (vw != null)
            {
                try
                {
                    // flush and put back into the pool
                    // don't close to allow us to play
                    // nicely with others.
                    vw.flush();
                    /* This hack sets the VelocityWriter's internal ref to the
                     * PrintWriter to null to keep memory free while
                     * the writer is pooled. See bug report #18951 */
                    vw.recycle(null);
                    writerPool.put(vw);
                }
                catch (Exception e)
                {
                    velocity.debug("VelocityViewServlet: " +
                                   "Trouble releasing VelocityWriter: " +
                                   e.getMessage());
                }
            }
        }
    }


    /**
     * This is here so developers may override it and gain access to the
     * Writer which the template will be merged into.  See
     * <a href="http://issues.apache.org/jira/browse/VELTOOLS-7">VELTOOLS-7</a>
     * for discussion of this.
     *
     * @param template template object returned by the handleRequest() method
     * @param context Context created by the {@link #createContext}
     * @param writer a VelocityWriter that the template is merged into
     */
    protected void performMerge(Template template, Context context, Writer writer)
        throws ResourceNotFoundException, ParseErrorException,
               MethodInvocationException, Exception
    {
        template.merge(context, writer);
    }


    /**
     * Invoked when there is an error thrown in any part of doRequest() processing.
     * <br><br>
     * Default will send a simple HTML response indicating there was a problem.
     *
     * @param request original HttpServletRequest from servlet container.
     * @param response HttpServletResponse object from servlet container.
     * @param e  Exception that was thrown by some other part of process.
     */
    protected void error(HttpServletRequest request,
                         HttpServletResponse response,
                         Exception e)
        throws ServletException
    {
        try
        {
            StringBuffer html = new StringBuffer();
            html.append("<html>\n");
            html.append("<head><title>Error</title></head>\n");
            html.append("<body>\n");
            html.append("<h2>VelocityViewServlet : Error processing a template for path '");
            html.append(org.apache.velocity.tools.view.servlet.ServletUtils.getPath(request));
            html.append("'</h2>\n");

            Throwable cause = e;

            String why = cause.getMessage();
            if (why != null && why.trim().length() > 0)
            {
                html.append(StringEscapeUtils.escapeHtml(why));
                html.append("\n<br>\n");
            }

            // if it's an MIE, i want the real stack trace!
            if (cause instanceof MethodInvocationException)
            {
                // get the real cause
                cause = ((MethodInvocationException)cause).getWrappedThrowable();
            }

            StringWriter sw = new StringWriter();
            cause.printStackTrace(new PrintWriter(sw));

            html.append("<pre>\n");
            html.append(StringEscapeUtils.escapeHtml(sw.toString()));
            html.append("</pre>\n");
            html.append("</body>\n");
            html.append("</html>");
            getResponseWriter(response).write(html.toString());
        }
        catch (Exception e2)
        {
            // clearly something is quite wrong.
            // let's log the new exception then give up and
            // throw a servlet exception that wraps the first one
            velocity.error("VelocityViewServlet: Exception while printing error screen: "+e2);
            throw new ServletException(e);
        }
    }

    /**
     * <p>Procure a Writer with correct encoding which can be used
     * even if HttpServletResponse's <code>getOutputStream()</code> method
     * has already been called.</p>
     *
     * <p>This is a transitional method which will be removed in a
     * future version of Velocity.  It is not recommended that you
     * override this method.</p>
     *
     * @param response The response.
     * @return A <code>Writer</code>, possibly created using the
     *        <code>getOutputStream()</code>.
     */
    protected Writer getResponseWriter(HttpServletResponse response)
        throws UnsupportedEncodingException, IOException
    {
        Writer writer = null;
        try
        {
            writer = response.getWriter();
        }
        catch (IllegalStateException e)
        {
            // ASSUMPTION: We already called getOutputStream(), so
            // calls to getWriter() fail.  Use of OutputStreamWriter
            // assures our desired character set
            if (this.warnOfOutputStreamDeprecation)
            {
                this.warnOfOutputStreamDeprecation = false;
                velocity.warn("VelocityViewServlet: " +
                              "Use of ServletResponse's getOutputStream() " +
                              "method with VelocityViewServlet is " +
                              "deprecated -- support will be removed in " +
                              "an upcoming release");
            }
            // Assume the encoding has been set via setContentType().
            String encoding = response.getCharacterEncoding();
            if (encoding == null)
            {
                encoding = DEFAULT_OUTPUT_ENCODING;
            }
            writer = new OutputStreamWriter(response.getOutputStream(),
                                            encoding);
        }
        return writer;
    }


    /**
     * Cleanup routine called at the end of the request processing sequence
     * allows a derived class to do resource cleanup or other end of
     * process cycle tasks.  This default implementation does nothing.
     *
     * @param request servlet request from client
     * @param response servlet reponse
     * @param context Context created by the {@link #createContext}
     */
    protected void requestCleanup(HttpServletRequest request,
                                  HttpServletResponse response,
                                  Context context)
    {
    }

}

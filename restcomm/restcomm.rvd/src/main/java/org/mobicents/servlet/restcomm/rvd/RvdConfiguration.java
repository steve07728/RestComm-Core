package org.mobicents.servlet.restcomm.rvd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;
import org.mobicents.servlet.restcomm.rvd.commons.http.SslMode;
import org.mobicents.servlet.restcomm.rvd.configuration.RestcommConfig;
import org.mobicents.servlet.restcomm.rvd.configuration.RvdXml;
import org.mobicents.servlet.restcomm.rvd.exceptions.callcontrol.CallControlException;
import org.mobicents.servlet.restcomm.rvd.model.client.WorkspaceSettings;
import org.mobicents.servlet.restcomm.rvd.utils.RvdUtils;
import org.w3c.dom.Document;

import com.thoughtworks.xstream.XStream;

public class RvdConfiguration {
    static final Logger logger = Logger.getLogger(RvdConfiguration.class.getName());
    private static RvdConfiguration instance = null;

    private static final String WORKSPACE_DIRECTORY_NAME = "workspace";
    public static final String PROTO_DIRECTORY_PREFIX = "_proto";
    public static final String REST_SERVICES_PATH = "services"; // the "services" from the /restcomm-rvd/services/apps/... path

    public static final String WAVS_DIRECTORY_NAME = "wavs";
    private static final String RVD_PROJECT_VERSION = "1.4"; // version for rvd project syntax
    private static final String PACKAGING_VERSION = "1.0";
    private static final String RAS_APPLICATION_VERSION = "2"; // version of the RAS application specification
    public static final String STICKY_PREFIX = "sticky_"; // a  prefix for rvd sticky variable names
    public static final String MODULE_PREFIX = "module_"; // a  prefix for rvd module-scoped variable names
    public static final String CORE_VARIABLE_PREFIX = "core_"; // a prefix for rvd variables that come from Restcomm parameters
    public static final String PACKAGING_DIRECTORY_NAME = "packaging";
    public static final String TICKET_COOKIE_NAME = "rvdticket"; // the name of the cookie that is used to store ticket ids for authentication
    private static Set<String> restcommParameterNames  = new HashSet<String>(Arrays.asList(new String[] {"CallSid","AccountSid","From","To","Body","CallStatus","ApiVersion","Direction","CallerName"})); // the names of the parameters supplied by restcomm request when starting an application
    public static final String PROJECT_LOG_FILENAME = "projectLog";
    public static final String DEFAULT_APPSTORE_DOMAIN = "apps.restcomm.com";
    public static final HashSet<String> builtinRestcommParameters = new HashSet<String>(Arrays.asList(new String[] {"CallSid","AccountSid","From","To","Body","CallStatus","ApiVersion","Direction","CallerName"}));
    public static final String RESTCOMM_HEADER_PREFIX = "SipHeader_"; // the prefix added to HTTP headers from Restcomm
    public static final String RESTCOMM_HEADER_PREFIX_DIAL = "DialSipHeader_"; // another prefix

    private String workspaceBasePath;
    private String externalServiceBase; // use this when relative urls (starting with /) are specified in ExternalService steps
    private String contextRootPath;
    private SslMode sslMode;

    private RvdXml rvdXml;  // the configuration settings from rvd.xml
    private RestcommConfig restcommConfig; // rvd-related configuration from restcomm.xml
    private WorkspaceSettings workspaceSettings;


    public static RvdConfiguration getInstance() {
        if ( instance == null ) {
            throw new IllegalStateException("RVD configuration has not been loaded.");
        }
        return instance;
    }

    public static RvdConfiguration createOnce(ServletContext servletContext) {
        synchronized (RvdConfiguration.class) {
            if ( instance == null ) {
                RvdConfiguration config = new RvdConfiguration(servletContext);
                config.load();
                instance = config;
            }
            return instance;
        }
    }

    private RvdConfiguration(ServletContext servletContext) {
        contextRootPath = servletContext.getRealPath("/");
    }

    // Use this constructor to manually build the configuration class when testing
    private RvdConfiguration(String contextRootPath) {
        this.contextRootPath = contextRootPath;
    }

    private void load() {
        // load configuration from rvd.xml file
        loadRvdXmlConfig(contextRootPath + "WEB-INF/rvd.xml");
        // load configuration from restcomm.xml file
        loadRestcommConfig(contextRootPath);
        // workspaceBasePath option
        String workspaceBasePath = contextRootPath + WORKSPACE_DIRECTORY_NAME;
        if (rvdXml.getWorkspaceLocation() != null  &&  !"".equals(rvdXml.getWorkspaceLocation()) ) {
            if ( rvdXml.getWorkspaceLocation().startsWith("/") )
                workspaceBasePath = rvdXml.getWorkspaceLocation(); // this is an absolute path
            else
                workspaceBasePath = contextRootPath + rvdXml.getWorkspaceLocation(); // this is a relative path hooked under RVD context
        }
        this.workspaceBasePath = workspaceBasePath;
        //  sslMode option
        sslMode = SslMode.allowall; // default
        if ( ! RvdUtils.isEmpty(rvdXml.getSslMode()) )
            sslMode = SslMode.valueOf(rvdXml.getSslMode());

        logger.info("Using workspace at " + workspaceBasePath);
    }

    /**
     * Loads rvd.xml into an RvdConfig.
     *
     * Returns null if the file is not found
     */
    private void loadRvdXmlConfig(String pathToXml) {
        try {
            FileInputStream input = new FileInputStream(pathToXml);
            XStream xstream = new XStream();
            xstream.alias("rvd", RvdXml.class);
            rvdXml = (RvdXml) xstream.fromXML( input );
        } catch (FileNotFoundException e) {
            logger.warn("RVD configuration file not found: " + pathToXml);
            return;
        }
    }

    /**
     * Reads restcomm.xml configuratin file and puts all important info into a RestcommConfig object
     *
     * Returns null if someting goes wrong and logs the error
     */
    private void loadRestcommConfig( String filesystemContextPath) {
        RestcommConfig config = new RestcommConfig();

        // Load restcomm configuration. Only the fields we are interested in. See RestcommXml model class
        String restcommConfigPath = filesystemContextPath + "../restcomm.war/WEB-INF/conf/restcomm.xml";
        File file = new File(restcommConfigPath);
        if ( !file.exists() ) {
            logger.error("Cannot find restcomm configuration file at: " + restcommConfigPath);
            return;
        }
        String recordingsUrl;
        try {
            recordingsUrl = extractRecordingsUrlFromRestcommConfig(file);
        } catch (Exception e) {
            logger.error("Error processing restcomm configuration from RVD.",e);
            return;
        }

        // Extract the settings we are interested in from the recordings url. We could also any other containing host and port information
        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder(recordingsUrl);
            config.setHost( uriBuilder.getHost() );
            config.setPort( uriBuilder.getPort() );
            this.restcommConfig = config;
        } catch (URISyntaxException e) {
            logger.error("Error extracting host and port information from recordings-uri in restcomm.xml: " + recordingsUrl);
            return;
        }
    }
    
    private void loadWorkspaceSettings() {
        if ( workspaceStorage.entityExists(".settings", "") ) {
            workspaceSettings = workspaceStorage.loadEntity(".settings", "", SettingsModel.class);
        } else
            workspaceSettings = null;
    }

    private String extractRecordingsUrlFromRestcommConfig(File file) throws Exception {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse (file);
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile("/restcomm/runtime-settings/recordings-uri/text()");
            String recordingsUrl = (String) expr.evaluate(doc, XPathConstants.STRING);

            return recordingsUrl.trim();
        } catch (Exception e) {
            throw new CallControlException("Error parsing restcomm config file: " + file.getPath(), e);
        }

    }

    public String getWorkspaceBasePath() {
        return this.workspaceBasePath;
    }

    public String getProjectBasePath(String projectName) {
        return this.workspaceBasePath + File.separator + projectName;
    }

    public static String getRvdProjectVersion() {
        return RVD_PROJECT_VERSION;
    }

    public static String getPackagingVersion() {
        return PACKAGING_VERSION;
    }

    public static String getRasApplicationVersion() {
        return RAS_APPLICATION_VERSION;
    }

    public String getExternalServiceBase() {
        return externalServiceBase;
    }

    public void setExternalServiceBase(String externalServiceBase) {
        this.externalServiceBase = externalServiceBase;
    }

    public String getEffectiveRestcommIp(HttpServletRequest request) {
        String ipFromXml = rvdXml.getRestcommPublicIp();

        String ip = request.getLocalAddr(); // use request ip as default
        if ( ipFromXml != null  &&  !"".equals(ipFromXml) ) {
            ip = ipFromXml;
        }
        return ip;
    }

    // Always returns the destination port in the request. When the configuration/settings scheme clears
    // out a proper implementation should be done.
    // TODO
    public static String getEffectiveRestcommPort(HttpServletRequest request) {
        int port = request.getLocalPort();
        return "" + port;
    }

    /**
     * Returns the IP rvd listens to for internal use. This address can be used by restcomm to access applications.
     * A request object is required to get it. Even when this request comes from a browser, this function should report
     * the correct IP.
     * @param request
     * @return
     */
    public static String getRvdInternalIp(HttpServletRequest request) {
        return request.getLocalAddr();
    }

    public static int getRvdInternalPort(HttpServletRequest request) {
        return request.getLocalPort();
    }

    public static Set<String> getRestcommParameterNames() {
        return restcommParameterNames;
    }

    public SslMode getSslMode() {
        return sslMode;
    }
    
    public String getRestcommHost() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public Integer getRestcommPort() {
        throw new UnsupportedOperationException("not yet implemented");
    }
}

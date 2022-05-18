package nz.xinsolutions.janitor;

import org.apache.jackrabbit.core.CheckerRepository;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.tika.io.IOUtils;
import org.onehippo.cms7.repository.checker.CheckerOptions;
import org.xml.sax.InputSource;

import javax.jcr.RepositoryException;
import java.io.*;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Author: Marnix Kok
 *
 * Purpose:
 *
 *      Run a check on a repository
 *
 */
public class LambdaHippoChecker {

    /**
     * Logger
     */
    private static final Logger LOG = Logger.getLogger("hippo-checker");

    /**
     * Stores the configuration
     */
    private HippoCheckerConfig config;

    /**
     * Initialise data-members
     *
     * @param config the configuration for the chcker
     */
    public LambdaHippoChecker(HippoCheckerConfig config) {
        this.config = config;
    }

    /**
     * Initialise the configuration files, especially the repository.xml by replacing
     * the placeholders with information from the configuration we have received from
     * the user.
     *
     * @throws IOException
     */
    public void initialise() throws IOException {
        // read dummy repository.xml
        InputStream stream = this.getClass().getResourceAsStream("/repository.xml");
        StringWriter strWriter = new StringWriter();
        IOUtils.copy(stream, strWriter);

        String repoXml = strWriter.toString();
        String finalRepoXml = (
            repoXml
                .replace("${host}", config.host)
                .replace("${port}", String.format("%d", config.port))
                .replace("${database}", config.database)
                .replace("${username}", config.username)
                .replace("${password}", config.password)
        );

        FileWriter fWriter = new FileWriter("/tmp/repository.xml");
        IOUtils.write(finalRepoXml, fWriter);
        fWriter.close();

        LOG.info("Initialisation complete.");
    }

    /**
     * Clean up version history.
     *
     * @throws IOException
     * @throws RepositoryException
     * @throws URISyntaxException
     */
    public void cleanupVersionHistory() throws IOException, RepositoryException, URISyntaxException {
        LOG.info("START: Starting to clean up version history in repository.");

        Properties properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("/checker.properties"));
        CheckerOptions options = new CheckerOptions(new String[] { "CLEANVH" }, properties);

        CheckerRepository repository = null;
        try {
            repository = createRepository(options, properties);
            repository.cleanHistory(options.cleanNonEmptyHistory());
        }
        finally {
            if (repository != null) {
                repository.shutdown();
            }
        }

        LOG.info("DONE: Finished cleaning up the version history");
    }

    /**
     * Clean up the data storage
     *
     * @throws IOException
     * @throws RepositoryException
     * @throws URISyntaxException
     */
    public void cleanupDataStorage()  throws IOException, RepositoryException, URISyntaxException {

        LOG.info("START: Starting to clean up data storage in repository.");

        Properties properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("/checker.properties"));
        CheckerOptions options = new CheckerOptions(new String[] { "CLEANDS" }, properties);

        CheckerRepository repository = null;
        try {
            repository = createRepository(options, properties);
            repository.cleanBinaries();
        }
        finally {
            if (repository != null) {
                repository.shutdown();
            }
        }

        LOG.info("DONE: Datastorage cleanup completed.");
    }


    /**
     * Create a checker repository instance based on the repository.xml written to /tmp by `initialise()`.
     *
     * @param options
     * @param properties
     * @return
     *
     * @throws RepositoryException
     * @throws URISyntaxException
     */
    protected CheckerRepository createRepository(CheckerOptions options, Properties properties) throws RepositoryException {
        InputSource inputSource = new InputSource("file:///tmp/repository.xml");
        return new CheckerRepository(RepositoryConfig.create(inputSource, properties), options);
    }

    /**
     * Run the checker
     */
    public static void run() {
        HippoCheckerConfig config = HippoCheckerConfig.fromEnvironmentVariables();
        LambdaHippoChecker checker = new LambdaHippoChecker(config);
        try {
            checker.initialise();
            checker.cleanupVersionHistory();
            checker.cleanupDataStorage();
        }
        catch (Exception ex) {
            LOG.warning("Could not execute hippo checker, caused by: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        run();
    }

}

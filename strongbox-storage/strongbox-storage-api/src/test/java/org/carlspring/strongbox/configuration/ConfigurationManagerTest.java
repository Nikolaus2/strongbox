package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.services.ArtifactResolutionService;
import org.carlspring.strongbox.xml.parsers.ConfigurationParser;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/META-INF/spring/strongbox-*-context.xml", "classpath*:/META-INF/spring/strongbox-*-context.xml"})
public class ConfigurationManagerTest
{

    public static final String TEST_CLASSES = "target/test-classes";

    public static final String CONFIGURATION_BASEDIR = TEST_CLASSES + "/xml";

    public static final String CONFIGURATION_OUTPUT_FILE = CONFIGURATION_BASEDIR + "/strongbox-saved-cm.xml";

    public static final String STORAGE_BASEDIR = TEST_CLASSES + "/storages/storage0";

    @Autowired
    private ConfigurationManager configurationManager;

    @Autowired
    private ArtifactResolutionService artifactResolutionService;


    @Before
    public void setUp()
            throws Exception
    {
        File xmlDir = new File(CONFIGURATION_BASEDIR);
        if (!xmlDir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            xmlDir.mkdirs();
        }
    }

    @Test
    public void testParseConfiguration()
            throws IOException
    {
        final Configuration configuration = configurationManager.getConfiguration();

        assertNotNull(configuration);
        assertNotNull(configuration.getStorages());

        for (String key : configuration.getStorages().keySet())
        {
            assertNotNull("Storage key was null!", key);
            assertTrue("No repositories were parsed!", !configuration.getStorages().get(key).getRepositories().isEmpty());
        }

        assertEquals("Unexpected number of storages!", 1, configuration.getStorages().size());
        assertEquals("Incorrect version!", "1.0", configuration.getVersion());
        assertEquals("Incorrect port number!", 48080, configuration.getPort());
        assertNotNull("No resolvers found!", artifactResolutionService.getResolvers());
        assertEquals("Incorrect number of resolvers found!", 2, artifactResolutionService.getResolvers().size());
        assertEquals("Repository should have required authentication!",
                     true,
                     configuration.getStorages().get("storage0").getRepositories().get("snapshots").isSecured());
    }

    @Test
    public void testStoreConfiguration()
            throws IOException
    {
        ProxyConfiguration proxyConfigurationGlobal = new ProxyConfiguration();
        proxyConfigurationGlobal.setUsername("maven");
        proxyConfigurationGlobal.setPassword("password");
        proxyConfigurationGlobal.setHost("192.168.100.1");
        proxyConfigurationGlobal.setPort(8080);
        proxyConfigurationGlobal.addNonProxyHost("192.168.100.1");
        proxyConfigurationGlobal.addNonProxyHost("192.168.100.2");

        ProxyConfiguration proxyConfigurationRepository1 = new ProxyConfiguration();
        proxyConfigurationRepository1.setUsername("maven");
        proxyConfigurationRepository1.setPassword("password");
        proxyConfigurationRepository1.setScheme(ProxyConfiguration.SCHEME_HTTPS);
        proxyConfigurationRepository1.setHost("192.168.100.5");
        proxyConfigurationRepository1.setPort(8080);
        proxyConfigurationRepository1.addNonProxyHost("192.168.100.10");
        proxyConfigurationRepository1.addNonProxyHost("192.168.100.11");

        Repository repository1 = new Repository("snapshots");
        repository1.setProxyConfiguration(proxyConfigurationRepository1);

        Repository repository2 = new Repository("releases");

        Storage storage = new Storage();
        storage.setBasedir(STORAGE_BASEDIR);
        storage.addRepository(repository1);
        storage.addRepository(repository2);

        Configuration configuration = new Configuration();
        configuration.addStorage(storage);
        configuration.setProxyConfiguration(proxyConfigurationGlobal);

        File outputFile = new File(CONFIGURATION_OUTPUT_FILE);

        ConfigurationParser parser = new ConfigurationParser();
        parser.store(configuration, outputFile.getCanonicalPath());

        assertTrue("Failed to store the produced XML!", outputFile.length() > 0);
    }

}

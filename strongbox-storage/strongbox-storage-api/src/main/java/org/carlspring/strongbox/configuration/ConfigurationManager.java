package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.xml.parsers.ConfigurationParser;

import javax.annotation.PostConstruct;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component
@Scope ("singleton")
public class ConfigurationManager
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);

    private String configurationPath;

    private Configuration configuration;

    @Autowired
    private ConfigurationResourceResolver configurationResourceResolver;


    public ConfigurationManager()
    {
    }

    @PostConstruct
    public void init()
            throws IOException
    {
        Resource resource = configurationResourceResolver.getConfigurationResource(ConfigurationResourceResolver.getBasedir() + "/etc/conf/strongbox.xml",
                                                                                   "repository.config.xml",
                                                                                   "etc/conf/strongbox.xml");

        logger.info("Loading Strongbox configuration from " + resource.toString() + "...");

        ConfigurationParser parser = new ConfigurationParser();

        configuration = parser.parse(resource.getInputStream());
        configuration.setResource(resource);

        dump();
    }

    public void dump()
    {
        System.out.println("Configuration version: " + configuration.getVersion());
        System.out.println("Listening on port: " + configuration.getPort());

        System.out.println("Loading storages...");
        for (String storageKey : configuration.getStorages().keySet())
        {
            System.out.println(" -> Storage: " + storageKey);
            Storage storage = configuration.getStorages().get(storageKey);
            for (String repositoryKey : storage.getRepositories().keySet())
            {
                System.out.println("    -> Repository: " + repositoryKey);
            }
        }
    }

    public void storeConfiguration(Configuration configuration, String file)
            throws IOException
    {
        ConfigurationParser parser = new ConfigurationParser();
        parser.store(configuration, file);
    }

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public String getConfigurationPath()
    {
        return configurationPath;
    }

    public void setConfigurationPath(String configurationPath)
    {
        this.configurationPath = configurationPath;
    }

}

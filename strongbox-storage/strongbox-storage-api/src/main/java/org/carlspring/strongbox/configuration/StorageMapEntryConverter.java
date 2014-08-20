package org.carlspring.strongbox.configuration;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import java.util.*;

/**
 * @author mtodorov
 */
public class StorageMapEntryConverter
        implements Converter
{

    private static final Logger logger = LoggerFactory.getLogger(StorageMapEntryConverter.class);


    public boolean canConvert(Class clazz)
    {
        return AbstractMap.class.isAssignableFrom(clazz);
    }

    public void marshal(Object value,
                        HierarchicalStreamWriter writer,
                        MarshallingContext context)
    {
        //noinspection unchecked
        Map<String, Storage> map = (LinkedHashMap<String, Storage>) value;

        for (Map.Entry<String, Storage> entry : map.entrySet())
        {
            Storage storage = entry.getValue();

            writer.startNode("storage");
            writer.startNode("basedir");
            writer.setValue(storage.getBasedir());
            writer.endNode();

            if (storage.getName() != null)
            {
                writer.startNode("name");
                writer.setValue(storage.getName());
                writer.endNode();
            }

            for (String repositoryKey : storage.getRepositories().keySet())
            {
                Repository repository = storage.getRepositories().get(repositoryKey);
                writeRepositoryNode(writer, repository);
            }

            writer.endNode();
        }
    }

    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context)
    {
        Map<String, Storage> map = new HashMap<String, Storage>();

        while (reader.hasMoreChildren())
        {
            reader.moveDown();

            Storage storage = new Storage();
            final String nodeName = reader.getNodeName();
            if (nodeName.equals("storage"))
            {
                while (reader.hasMoreChildren())
                {
                    reader.moveDown();

                    if (reader.getNodeName().equals("name"))
                    {
                        storage.setName(reader.getValue().trim());
                    }
                    else if (reader.getNodeName().equals("basedir"))
                    {
                        storage.setBasedir(reader.getValue().trim());
                    }
                    else if (reader.getNodeName().equals("repositories"))
                    {
                        final Map<String, Repository> repositories = parseRepositories(reader, context, storage);
                        storage.setRepositories(repositories);
                    }
                    else
                    {
                        logger.warn("Not parsing node " + reader.getNodeName());
                    }

                    reader.moveUp();
                }

                map.put(storage.getName(), storage);
            }

            reader.moveUp();
        }

        return map;
    }

    private Map<String, Repository> parseRepositories(HierarchicalStreamReader reader,
                                                      UnmarshallingContext context,
                                                      Storage storage)
    {
        Map<String, Repository> repositories = new LinkedHashMap<String, Repository>();

        while (reader.hasMoreChildren())
        {
            reader.moveDown();

            Repository repository = (Repository) context.convertAnother(null, Repository.class);
            repository.setStorage(storage);
            if (repository.acceptsSnapshots())
            {
                // Make sure it's possible to re-deploy SNAPSHOT artifacts,
                // if this is a repository with a SNAPSHOT policy:
                repository.setAllowsRedeployment(true);
            }
            repositories.put(repository.getName(), repository);

            reader.moveUp();
        }

        return repositories;
    }

    private void writeRepositoryNode(HierarchicalStreamWriter writer,
                                     Repository repository)
    {
        writer.startNode("repository");

        writer.addAttribute("name", repository.getName());
        writer.addAttribute("implementation", repository.getImplementation());
        writer.addAttribute("policy", repository.getPolicy());
        writer.addAttribute("type", repository.getType());

        final RemoteRepository remoteRepository = repository.getRemoteRepository();
        if (remoteRepository != null)
        {
            if (remoteRepository.getUrl() != null)
            {
                writer.startNode("url");
                writer.setValue(remoteRepository.getUrl());
                writer.endNode();
            }

            if (!StringUtils.isEmpty(remoteRepository.getUsername()))
            {
                writer.startNode("username");
                writer.setValue(remoteRepository.getUsername());
                writer.endNode();
            }

            if (!StringUtils.isEmpty(remoteRepository.getPassword()))
            {
                writer.startNode("password");
                writer.setValue(remoteRepository.getPassword());
                writer.endNode();
            }

            if (!StringUtils.isEmpty(remoteRepository.getChecksumPolicy()))
            {
                writer.startNode("checksum-policy");
                writer.setValue(remoteRepository.getChecksumPolicy());
                writer.endNode();
            }

            writer.startNode("download-remote-indexes");
            writer.setValue(Boolean.toString(remoteRepository.isDownloadRemoteIndexes()));
            writer.endNode();

            writer.startNode("auto-blocking");
            writer.setValue(Boolean.toString(remoteRepository.isAutoBlocking()));
            writer.endNode();

            writer.startNode("checksum-validation");
            writer.setValue(Boolean.toString(remoteRepository.isChecksumValidation()));
            writer.endNode();

        }

        final ProxyConfiguration proxyConfiguration = repository.getProxyConfiguration();
        if (proxyConfiguration != null)
        {
            writer.startNode("proxy-configuration");

            if (proxyConfiguration.getHost() != null)
            {
                writer.startNode("host");
                writer.setValue(proxyConfiguration.getHost());
                writer.endNode();
            }

            if (proxyConfiguration.getPort() > 0)
            {
                writer.startNode("port");
                writer.setValue(Integer.toString(proxyConfiguration.getPort()));
                writer.endNode();
            }

            if (proxyConfiguration.getUsername() != null)
            {
                writer.startNode("username");
                writer.setValue(proxyConfiguration.getUsername());
                writer.endNode();
            }

            if (proxyConfiguration.getPassword() != null)
            {
                writer.startNode("password");
                writer.setValue(proxyConfiguration.getPassword());
                writer.endNode();
            }

            final List<String> nonProxyHosts = proxyConfiguration.getNonProxyHosts();
            if (nonProxyHosts != null && !nonProxyHosts.isEmpty())
            {
                writer.startNode("non-proxy-hosts");

                for (String nonProxyHost : nonProxyHosts)
                {
                    writer.startNode("non-proxy-host");
                    writer.setValue(nonProxyHost);
                    writer.endNode();
                }

                writer.endNode();
            }

            writer.endNode();
        }

        writer.endNode();
    }

}
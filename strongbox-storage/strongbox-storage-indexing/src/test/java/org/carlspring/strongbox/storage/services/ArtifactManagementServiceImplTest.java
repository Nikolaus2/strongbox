package org.carlspring.strongbox.storage.services;

import org.apache.maven.artifact.Artifact;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.generator.ArtifactGenerator;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.indexing.SearchRequest;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/META-INF/spring/strongbox-*-context.xml",
                                    "classpath*:/META-INF/spring/strongbox-*-context.xml" })
public class ArtifactManagementServiceImplTest
{

    private static final File REPOSITORY_BASEDIR = new File("target/storages/storage0/releases");

    private static final File INDEX_DIR = new File(REPOSITORY_BASEDIR, ".index");

    @Autowired
    private ArtifactManagementService artifactManagementService;

    @Autowired
    private ArtifactSearchService artifactSearchService;

    @Autowired
    private RepositoryIndexManager repositoryIndexManager;

    @Before
    public void init()
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException
    {
        //noinspection ResultOfMethodCallIgnored
        INDEX_DIR.mkdirs();

        Artifact artifact1 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:strongbox-utils:6.0.1:jar");
        Artifact artifact2 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:strongbox-utils:6.1.1:jar");
        Artifact artifact3 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:strongbox-utils:6.2.1:jar");

        ArtifactGenerator generator = new ArtifactGenerator(REPOSITORY_BASEDIR.getAbsolutePath());
        generator.generate(artifact1);
        generator.generate(artifact2);
        generator.generate(artifact3);
    }

    @Test
    public void testMerge()
            throws Exception
    {
        final RepositoryIndexer repositoryIndexer = repositoryIndexManager.getRepositoryIndex("storage0:releases");
        final int x = repositoryIndexer.index(new File("org/carlspring/strongbox/strongbox-utils"));

        SearchRequest request = new SearchRequest("storage0",
                                                  "releases",
                                                  "g:org.carlspring.strongbox a:strongbox-utils v:6.2.1 p:jar");

        Assert.assertTrue(artifactSearchService.contains(request));

        request = new SearchRequest("storage0",
                                    "releases-with-trash",
                                    "g:org.carlspring.strongbox a:strongbox-utils v:6.2.1 p:jar");

        Assert.assertFalse(artifactSearchService.contains(request));

        artifactManagementService.merge("storage0", "releases", "storage0", "releases-with-trash");

        request = new SearchRequest("storage0",
                                    "releases-with-trash",
                                    "g:org.carlspring.strongbox a:strongbox-utils v:6.2.1 p:jar");

        Assert.assertTrue(artifactSearchService.contains(request));
    }

}

package org.carlspring.strongbox.storage.services;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.generator.ArtifactGenerator;
import org.carlspring.strongbox.client.ArtifactOperationException;
import org.carlspring.strongbox.storage.services.impl.ArtifactMetadataServiceImpl;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author stodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.JVM)
@ContextConfiguration(locations = {"/META-INF/spring/strongbox-*-context.xml",
                                   "classpath*:/META-INF/spring/strongbox-*-context.xml"})
public class ArtifactMetadataServiceImplTest
{

    private static final File REPOSITORY_BASEDIR = new File("target/storages/storage0/releases");

    private static final Artifact ARTIFACT = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:strongbox-metadata:1.0:jar");
    private static final Artifact LATEST_ARTIFACT = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:strongbox-metadata:3.0-SNAPSHOT:jar");
    private static final Artifact RELEASE_ARTIFACT = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:strongbox-metadata:2.0:jar");

    @Autowired
    private ArtifactMetadataServiceImpl artifactMetadataService;


    @Before
    public void setUp()
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException,
                   ArtifactOperationException
    {
        if (!new File(REPOSITORY_BASEDIR, "org/carlspring/strongbox/strongbox-metadata").exists())
        {
            //noinspection ResultOfMethodCallIgnored
            REPOSITORY_BASEDIR.mkdirs();

            ArtifactGenerator generator = new ArtifactGenerator(REPOSITORY_BASEDIR.getAbsolutePath());
            generator.generate(ARTIFACT);
            generator.generate(ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:strongbox-metadata:1.1:jar"));
            generator.generate(RELEASE_ARTIFACT);
            generator.generate(LATEST_ARTIFACT);

            artifactMetadataService.rebuildMetadata("storage0", "releases", ARTIFACT);
        }
    }

    @Test
    public void testMetadataRebuild()
            throws IOException, XmlPullParserException
    {
        String artifactBasePath = artifactMetadataService.getArtifactBasePath("storage0", "releases", ARTIFACT).toString();
        File metadataFile = new File(artifactBasePath + "/maven-metadata.xml");

        Assert.assertTrue("Failed to rebuild maven-metadata.xml file in " + artifactBasePath, metadataFile.exists());
    }

    @Test
    public void testMetadataRead()
            throws IOException, XmlPullParserException
    {
        Metadata metadata = artifactMetadataService.getMetadata("storage0", "releases", ARTIFACT);
        Versioning versioning = metadata.getVersioning();

        Assert.assertEquals("Incorrect artifactId!", ARTIFACT.getArtifactId(), metadata.getArtifactId());
        Assert.assertEquals("Incorrect groupId!", ARTIFACT.getGroupId(), metadata.getGroupId());
        Assert.assertEquals("Incorrect latest version!", LATEST_ARTIFACT.getVersion(), versioning.getLatest());
        Assert.assertEquals("Incorrect release version!", RELEASE_ARTIFACT.getVersion(), versioning.getRelease());
    }

}

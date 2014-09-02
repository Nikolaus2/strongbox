package org.carlspring.strongbox.storage.visitors;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

/**
 * @author stodorov
 */
public class ArtifactPomVisitor
        extends SimpleFileVisitor<Path>
{

    private PathMatcher matcher;

    public ArrayList<Path> foundPaths = new ArrayList<>();


    public ArtifactPomVisitor()
    {
        matcher = FileSystems.getDefault().getPathMatcher("glob:*.pom");
    }

    @Override
    public FileVisitResult visitFile(Path file,
                                     BasicFileAttributes attr)
            throws IOException
    {
        Path name = file.getFileName();
        if (matcher.matches(name))
        {
            foundPaths.add(file);
        }

        return FileVisitResult.CONTINUE;
    }

}

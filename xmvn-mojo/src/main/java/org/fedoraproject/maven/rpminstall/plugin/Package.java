/*-
 * Copyright (c) 2012 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fedoraproject.maven.rpminstall.plugin;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.artifact.Artifact;
import org.fedoraproject.maven.Configuration;

public class Package
    implements Comparable<Package>
{
    private final String suffix;

    private boolean pureDevelPackage = true;

    public Package( String name )
    {
        suffix = name.equals( "" ) ? "" : "-" + name;
    }

    private final FragmentFile depmap = new FragmentFile();

    class TargetFile
    {
        File sourceFile;

        String dirPath;

        String targetName;
    }

    private final List<TargetFile> targetFiles = new LinkedList<>();

    public void addFile( File file, String dirPath, String fileName )
    {
        pureDevelPackage = false;

        TargetFile target = new TargetFile();
        target.sourceFile = file;
        target.dirPath = dirPath;
        target.targetName = fileName;
        targetFiles.add( target );
    }

    public void addPomFile( File file, Artifact artifact )
    {
        String jppGroupId = "JPP/" + Configuration.getInstallName();
        String jppArtifactId = artifact.getArtifactId();
        String pomName = jppGroupId.replace( '/', '.' ) + "-" + jppArtifactId + ".pom";
        addFile( file, Configuration.getInstallPomDir(), pomName );

        addDepmap( artifact );
    }

    private static boolean containsNativeCode( File jar )
    {
        // TODO: implement
        return false;
    }

    public void addJarFile( File file, Artifact artifact )
    {
        String jarDir =
            containsNativeCode( file ) ? Configuration.getInstallJniDir() : Configuration.getInstallJarDir();
        addFile( file, jarDir + "/" + Configuration.getInstallName(), artifact.getArtifactId() + ".jar" );

        depmap.addJavaVersionRequirement( "1.5" );
    }

    private void installFiles( Installer installer )
        throws IOException
    {
        for ( TargetFile target : targetFiles )
        {
            installer.installFile( target.sourceFile, target.dirPath, target.targetName );
        }
    }

    public void addDepmap( Artifact artifact )
    {
        String groupId = artifact.getGroupId();
        String artifactId = artifact.getArtifactId();
        String version = artifact.getVersion();

        depmap.addMapping( groupId, artifactId, version, "JPP/" + Configuration.getInstallName(), artifactId );
    }

    public void addRequires( String groupId, String artifactId )
    {
        depmap.addDependency( groupId, artifactId );
    }

    public void addDevelRequires( String groupId, String artifactId )
    {
        depmap.addDevelDependency( groupId, artifactId );
    }

    private void installMetadata( Installer installer )
        throws IOException
    {
        depmap.optimize();

        if ( !depmap.isEmpty() )
        {
            File file = File.createTempFile( "xmvn", ".xml" );
            depmap.write( file, pureDevelPackage );
            String depmapName = Configuration.getInstallName() + suffix + ".xml";
            addFile( file, Configuration.getInstallDepmapDir(), depmapName );
        }
    }

    private void createFileList()
        throws IOException
    {
        Set<String> targetNames = new TreeSet<>();
        for ( TargetFile target : targetFiles )
        {
            File file = new File( target.dirPath, target.targetName );
            targetNames.add( file.getPath() );
        }

        PrintStream ps = new PrintStream( ".mfiles" + suffix );
        for ( String path : targetNames )
        {
            ps.println( "/" + path );
        }
        ps.close();
    }

    public void install( Installer installer )
        throws IOException
    {
        installMetadata( installer );
        installFiles( installer );
        createFileList();
    }

    @Override
    public int compareTo( Package rhs )
    {
        return suffix.compareTo( rhs.suffix );
    }
}

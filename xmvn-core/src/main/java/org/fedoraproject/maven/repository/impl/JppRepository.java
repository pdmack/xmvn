/*-
 * Copyright (c) 2012-2013 Red Hat, Inc.
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
package org.fedoraproject.maven.repository.impl;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.codehaus.plexus.component.annotations.Component;
import org.fedoraproject.maven.repository.Repository;

/**
 * JPP-style repository JPP layout, either versioned or versionless, depending on properties.
 * <p>
 * Example: {@code g/r/o/u/p/artifact-ver.ext} or {@code g/r/o/u/p/artifact.ext}
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = Repository.class, hint = "jpp", instantiationStrategy = "per-lookup" )
public class JppRepository
    extends SimpleRepository
{
    @Override
    protected Path getArtifactPath( String groupId, String artifactId, String extension, String classifier,
                                    String version )
    {
        StringBuilder path = new StringBuilder();

        if ( groupId.startsWith( "JPP/" ) )
            path.append( groupId.substring( 4 ) ).append( '/' );
        else if ( !groupId.equals( "JPP" ) )
            path.append( groupId ).append( '/' );

        path.append( artifactId );

        if ( version != null )
            path.append( '-' ).append( version );

        if ( !classifier.isEmpty() )
            path.append( '-' ).append( classifier );

        if ( !extension.isEmpty() )
            path.append( '.' ).append( extension );

        return Paths.get( path.toString() );
    }
}
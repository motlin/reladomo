/*
 Copyright 2016 Goldman Sachs.
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

package com.gs.fw.common.mithra.maven;

import com.gs.fw.common.freyaxml.generator.FreyaXmlGenerator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * Generates Java beans from XSD schemas using xsd2beangen (FreyaXml).
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class Xsd2BeanMojo extends AbstractMojo
{
    @Parameter(required = true)
    private String parserName;

    @Parameter(required = true)
    private String destinationPackage;

    @Parameter(required = true)
    private File xsd;

    @Parameter(required = true)
    private File generatedDir;

    @Parameter(required = true)
    private File nonGeneratedDir;

    @Parameter(defaultValue = "false")
    private boolean generateTopLevelSubstitutionElements;

    @Parameter(defaultValue = "false")
    private boolean ignoreNonGeneratedAbstractClasses;

    @Parameter(defaultValue = "false")
    private boolean ignorePackageNamingConvention;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException
    {
        if (!xsd.exists())
        {
            throw new MojoExecutionException("XSD file does not exist: " + xsd.getAbsolutePath());
        }

        generatedDir.mkdirs();
        nonGeneratedDir.mkdirs();

        getLog().info("Generating xsd2bean classes from " + xsd.getName());
        getLog().info("  parserName: " + parserName);
        getLog().info("  destinationPackage: " + destinationPackage);
        getLog().info("  generatedDir: " + generatedDir);

        try
        {
            FreyaXmlGenerator generator = new FreyaXmlGenerator();
            generator.setLogger(new FreyaMavenLogger(getLog()));
            generator.setParserName(parserName);
            generator.setDestinationPackage(destinationPackage);
            generator.setGeneratedDir(generatedDir.getAbsolutePath());
            generator.setNonGeneratedDir(nonGeneratedDir.getAbsolutePath());
            generator.setXsd(xsd.getAbsolutePath());
            generator.setGenerateTopLevelSubstitutionElements(generateTopLevelSubstitutionElements);
            generator.setIgnoreNonGeneratedAbstractClasses(ignoreNonGeneratedAbstractClasses);
            generator.setIgnorePackageNamingConvention(ignorePackageNamingConvention);
            generator.generate();
        }
        catch (Exception e)
        {
            throw new MojoExecutionException("xsd2bean generation failed", e);
        }

        if (project != null)
        {
            project.addCompileSourceRoot(generatedDir.getAbsolutePath());
        }
    }
}

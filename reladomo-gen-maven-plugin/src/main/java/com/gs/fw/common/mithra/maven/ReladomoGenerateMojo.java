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

import com.gs.fw.common.mithra.generator.CoreMithraGenerator;
import com.gs.fw.common.mithra.generator.MithraGeneratorImport;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;

/**
 * Generates Reladomo domain classes from Reladomo XML definitions.
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class ReladomoGenerateMojo extends AbstractMojo
{
    @Parameter(required = true)
    private File xml;

    @Parameter(required = true)
    private File generatedDir;

    @Parameter(required = true)
    private File nonGeneratedDir;

    @Parameter(defaultValue = "true")
    private boolean generateEcListMethod;

    @Parameter(defaultValue = "false")
    private boolean generateConcreteClasses;

    @Parameter(defaultValue = "false")
    private boolean forceOffHeap;

    @Parameter(defaultValue = "false")
    private boolean ignoreNonGeneratedAbstractClasses;

    @Parameter(defaultValue = "false")
    private boolean ignoreTransactionalMethods;

    @Parameter(defaultValue = "false")
    private boolean defaultFinalGetters;

    @Parameter(defaultValue = "false")
    private boolean generateImported;

    @Parameter
    private List<MithraImport> mithraImports;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException
    {
        if (!xml.exists())
        {
            throw new MojoExecutionException("Reladomo XML file does not exist: " + xml.getAbsolutePath());
        }

        generatedDir.mkdirs();
        nonGeneratedDir.mkdirs();

        getLog().info("Generating Reladomo classes from " + xml.getName());
        getLog().info("  generatedDir: " + generatedDir);
        getLog().info("  nonGeneratedDir: " + nonGeneratedDir);

        try
        {
            CoreMithraGenerator generator = new CoreMithraGenerator();
            generator.setLogger(new MavenReladomoLogger(getLog()));
            generator.setXml(xml.getAbsolutePath());
            generator.setGeneratedDir(generatedDir.getAbsolutePath());
            generator.setNonGeneratedDir(nonGeneratedDir.getAbsolutePath());
            generator.setGenerateEcListMethod(generateEcListMethod);
            generator.setGenerateConcreteClasses(generateConcreteClasses);
            generator.setForceOffHeap(forceOffHeap);
            generator.setIgnoreNonGeneratedAbstractClasses(ignoreNonGeneratedAbstractClasses);
            generator.setIgnoreTransactionalMethods(ignoreTransactionalMethods);
            generator.setDefaultFinalGetters(defaultFinalGetters);
            generator.setGenerateImported(generateImported);

            if (mithraImports != null)
            {
                for (MithraImport mithraImport : mithraImports)
                {
                    MithraGeneratorImport generatorImport = new MithraGeneratorImport();
                    generatorImport.setDir(mithraImport.getDir());
                    generatorImport.setFilename(mithraImport.getFilename());
                    generator.addConfiguredMithraImport(generatorImport);
                }
            }

            generator.execute();
        }
        catch (Exception e)
        {
            throw new MojoExecutionException("Reladomo code generation failed", e);
        }

        if (project != null)
        {
            project.addCompileSourceRoot(generatedDir.getAbsolutePath());
        }
    }
}

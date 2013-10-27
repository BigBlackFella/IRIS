package com.temenos.interaction.sdk.plugin;

/*
 * #%L
 * interaction-sdk-rim-plugin Maven Mojo
 * %%
 * Copyright (C) 2012 - 2013 Temenos Holdings N.V.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.google.inject.Injector;
import com.temenos.interaction.rimdsl.RIMDslStandaloneSetup;
import com.temenos.interaction.rimdsl.RIMDslStandaloneSetupSwagger;
import com.temenos.interaction.rimdsl.generator.launcher.Generator;

import java.io.File;

/**
 * Goal which generates Java classes from a RIM.
 *
 * @goal rim-generate
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class RIMGeneratorMojo extends AbstractMojo {

    /**
     * Location of the RIM source directory.
     * @parameter
     */
    private File rimSourceDir;

    /**
     * Location of the RIM file.
     * @parameter
     */
    private File rimSourceFile;

	/**
     * Output location for generated Java code.
     * @parameter expression="${project.build.directory}/src-gen"
     */
    private File targetDirectory;
    
    /**
     * Skip the RIM generation.  Effectively disable this plugin.
     * @parameter
     */
    private boolean skipRIMGeneration;

	/**
     * Output location for generated Swagger docs.
     * @parameter expression="${project.build.directory}/swagger-gen"
     */
    private File swaggerTargetDirectory;
    
    /**
     * Skip the Swagger generation.
     * @parameter default-value="true"
     */
    private boolean skipSwaggerGeneration;
    
    public void setRimSourceDir(File rimSourceDir) {
		this.rimSourceDir = rimSourceDir;
	}

    public void setRimSourceFile(File rimSourceFile) {
		this.rimSourceFile = rimSourceFile;
	}

	public void setTargetDirectory(File targetDirectory) {
		this.targetDirectory = targetDirectory;
	}

    public void setSkipRIMGeneration(String skipRIMGeneration) {
		this.skipRIMGeneration = (skipRIMGeneration != null && skipRIMGeneration.equalsIgnoreCase("true"));
	}

    public void setSwaggerTargetDirectory(File swaggerTargetDirectory) {
		this.swaggerTargetDirectory = swaggerTargetDirectory;
	}

	public void setSkipSwaggerGeneration(String skipSwaggerGeneration) {
		this.skipSwaggerGeneration = (skipSwaggerGeneration != null && skipSwaggerGeneration.equalsIgnoreCase("true"));
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
    	if (!skipRIMGeneration || !skipSwaggerGeneration) {
    		// check our configuration
    		if (rimSourceFile == null && rimSourceDir == null)
    			throw new MojoExecutionException("Neither [rimSourceFile] nor [rimSourceDir] specified in plugin configuration");
    		if (rimSourceFile != null && rimSourceDir != null)
    			throw new MojoExecutionException("Cannot configure [rimSourceFile] and [rimSourceDir] in plugin configuration");
    	}
    	
    	boolean ok = false;
    	if (skipRIMGeneration) {
    		getLog().info("[skipRIMGeneration] set, not generating any source");
    		ok = true;
    	} else {
    		if (targetDirectory == null) {
    			throw new MojoExecutionException("[targetDirectory] is set to null plugin configuration");
    		}

    		if (!targetDirectory.exists()) {
    			getLog().info("Creating [targetDirectory] " + targetDirectory.toString());
    			targetDirectory.mkdirs();
    		}
    		Injector injector = new RIMDslStandaloneSetup().createInjectorAndDoEMFRegistration();
    		Generator generator = injector.getInstance(Generator.class);
    		if (rimSourceDir != null) {
        		ok = generator.runGeneratorDir(rimSourceDir.toString(), targetDirectory.toString());
    		} else {
        		ok = generator.runGenerator(rimSourceFile.toString(), targetDirectory.toString());
    		}
    	}

    	if (!ok)
			throw new MojoFailureException("An unexpected error occurred while generating source");

    	if (skipSwaggerGeneration) {
    		getLog().info("[skipSwaggerGeneration] set, not generating swagger documents");
    		ok = true;
    	} else {
    		if (!swaggerTargetDirectory.exists()) {
    			getLog().info("Creating [swaggerTargetDirectory] " + swaggerTargetDirectory.toString());
    			swaggerTargetDirectory.mkdirs();
    		}
    		Injector injector = new RIMDslStandaloneSetupSwagger().createInjectorAndDoEMFRegistration();
    		Generator generator = injector.getInstance(Generator.class);
    		if (rimSourceDir != null) {
        		ok = generator.runGeneratorDir(rimSourceDir.toString(), swaggerTargetDirectory.toString());
    		} else {
        		ok = generator.runGenerator(rimSourceFile.toString(), swaggerTargetDirectory.toString());
    		}
    	}

    	if (!ok)
			throw new MojoFailureException("An unexpected error occurred while generating source");

    }
}

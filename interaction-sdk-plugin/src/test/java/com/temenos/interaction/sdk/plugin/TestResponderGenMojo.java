package com.temenos.interaction.sdk.plugin;

/*
 * #%L
 * interaction-sdk-plugin Maven Mojo
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


import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
//import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.powermock.api.support.membermodification.MemberMatcher.constructor;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.temenos.interaction.sdk.JPAResponderGen;
import com.temenos.interaction.sdk.adapter.edmx.EDMXAdapter;

@Ignore // jdk8 upgrade issue - java.lang.ClassNotFoundException: org.eclipse.xtext.serializer.ISerializationContext
@RunWith(PowerMockRunner.class)
@PrepareForTest(ResponderGenMojo.class)
public class TestResponderGenMojo {

	@Test(expected = MojoExecutionException.class)
	public void testMojoNoConfiguration() throws MojoExecutionException, MojoFailureException {
		ResponderGenMojo rgm = new ResponderGenMojo();
		rgm.execute();
	}

	@Test(expected = MojoExecutionException.class)
	public void testMojoSrcTargetDirConfiguration() throws MojoExecutionException, MojoFailureException {
		ResponderGenMojo rgm = new ResponderGenMojo();
		rgm.setEdmxFile("blah.edmx");
		rgm.execute();
	}

	@Test
	public void testMojoConfigurationConfigSrc() throws Exception {
		suppress(method(ResponderGenMojo.class, "execute", File.class, File.class, File.class));
		ResponderGenMojo rgm = new ResponderGenMojo();
		rgm.setEdmxFile("blah.edmx");
		rgm.setSrcTargetDirectory("./tmp");
		
		File fileMock = mock(File.class);
		whenNew(File.class).withArguments(String.class).thenReturn(fileMock);
		
		rgm.execute();
		verifyNew(File.class, times(1)).withArguments("blah.edmx");
		// verify that config directory is set to src directory
		verifyNew(File.class, times(2)).withArguments("./tmp");
	}

	@Test(expected = MojoExecutionException.class)
	public void testMojoExecuteEDMXFileNotFound() throws Exception {
		suppress(method(JPAResponderGen.class, "generateArtifacts", EDMXAdapter.class, File.class, File.class));
		
		ResponderGenMojo rgm = new ResponderGenMojo();
		
		File edmxFileMock = mock(File.class);
		File srcDirMock = mock(File.class);
		File configDirMock = mock(File.class);
		
		when(edmxFileMock.exists()).thenReturn(false);
		
		rgm.execute(edmxFileMock, srcDirMock, configDirMock);
	}

	@Test
	public void testMojoExecuteSrcNotExist() throws Exception {
		suppress(constructor(EDMXAdapter.class, String.class));
		ResponderGenMojo rgm = new ResponderGenMojo();
		
		File edmxFileMock = mock(File.class);
		File srcDirMock = mock(File.class);
		File configDirMock = mock(File.class);
		
		when(edmxFileMock.exists()).thenReturn(true);
		when(srcDirMock.exists()).thenReturn(false);
		when(srcDirMock.isDirectory()).thenReturn(true);
		when(configDirMock.exists()).thenReturn(true);
		when(configDirMock.isDirectory()).thenReturn(true);
		
		JPAResponderGen mockResponderGen = mock(JPAResponderGen.class);
		when(mockResponderGen.generateArtifacts(any(EDMXAdapter.class), any(File.class), any(File.class))).thenReturn(true);
		whenNew(JPAResponderGen.class).withArguments(any(boolean.class)).thenReturn(mockResponderGen);

		rgm.execute(edmxFileMock, srcDirMock, configDirMock);
		Mockito.verify(srcDirMock, times(1)).mkdirs();
		Mockito.verify(configDirMock, times(0)).mkdirs();
	}

	@Test(expected = MojoExecutionException.class)
	public void testMojoExecuteSrcNotDir() throws Exception {
		suppress(method(JPAResponderGen.class, "generateArtifacts", EDMXAdapter.class, File.class, File.class));
		
		ResponderGenMojo rgm = new ResponderGenMojo();
		
		File edmxFileMock = mock(File.class);
		File srcDirMock = mock(File.class);
		File configDirMock = mock(File.class);
		
		when(edmxFileMock.exists()).thenReturn(true);
		when(srcDirMock.exists()).thenReturn(true);
		
		rgm.execute(edmxFileMock, srcDirMock, configDirMock);
		
	}

	@Test
	public void testMojoExecuteConfigNotExist() throws Exception {
		suppress(constructor(EDMXAdapter.class, String.class));
		ResponderGenMojo rgm = new ResponderGenMojo();
		
		File edmxFileMock = mock(File.class);
		File srcDirMock = mock(File.class);
		File configDirMock = mock(File.class);
		
		when(edmxFileMock.exists()).thenReturn(true);
		when(srcDirMock.exists()).thenReturn(true);
		when(srcDirMock.isDirectory()).thenReturn(true);
		when(configDirMock.exists()).thenReturn(false);
		when(configDirMock.isDirectory()).thenReturn(true);
		
		JPAResponderGen mockResponderGen = mock(JPAResponderGen.class);
		when(mockResponderGen.generateArtifacts(any(EDMXAdapter.class), any(File.class), any(File.class))).thenReturn(true);
		whenNew(JPAResponderGen.class).withArguments(any(boolean.class)).thenReturn(mockResponderGen);
		
		rgm.execute(edmxFileMock, srcDirMock, configDirMock);
		Mockito.verify(srcDirMock, times(0)).mkdirs();
		Mockito.verify(configDirMock, times(1)).mkdirs();
	}

	@Test(expected = MojoExecutionException.class)
	public void testMojoExecuteConfigNotDir() throws Exception {
		suppress(method(JPAResponderGen.class, "generateArtifacts", EDMXAdapter.class, File.class, File.class));
		
		ResponderGenMojo rgm = new ResponderGenMojo();
		
		File edmxFileMock = mock(File.class);
		File srcDirMock = mock(File.class);
		File configDirMock = mock(File.class);
		
		when(edmxFileMock.exists()).thenReturn(true);
		when(srcDirMock.exists()).thenReturn(true);
		when(srcDirMock.isDirectory()).thenReturn(true);
		when(configDirMock.exists()).thenReturn(true);
		when(configDirMock.isDirectory()).thenReturn(false);
		
		rgm.execute(edmxFileMock, srcDirMock, configDirMock);
		
	}
}

package com.temenos.interaction.core.resource;

/*
 * #%L
 * interaction-core
 * %%
 * Copyright (C) 2012 - 2015 Temenos Holdings N.V.
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


import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.junit.Test;

public class TestConfigLoader {

	@Test
	public void testIsExistClasspath() {
		ConfigLoader loader = new ConfigLoader();
		boolean found = loader.isExist("metadata-CountryList.xml");
		assertTrue(found);
	}

	@Test
	public void testNotFoundIsExistClasspath() {
		ConfigLoader loader = new ConfigLoader();
		boolean found = loader.isExist("somefilethatdoesnotexist");
		assertFalse(found);
	}

	@Test
	public void testLoadClasspath() throws Exception {
		ConfigLoader loader = new ConfigLoader();
		InputStream in = loader.load("metadata-CountryList.xml");
		assertNotNull(in);
	}

	@Test
	public void testSetIrisConfigDirPathSingle() throws IOException {
		Path directory = null;
		try {
			directory = Files.createTempDirectory("someName");
			ConfigLoader loader = new ConfigLoader();
			loader.setIrisConfigDirPath(directory.toString());
			assertEquals(1, loader.getIrisConfigDirPaths().size());
			assertEquals(directory.toString(), loader.getIrisConfigDirPaths().iterator().next());
		} finally {
			Files.deleteIfExists(directory);
		}
	}

	@Test
	public void testSetIrisConfigDirPathMultiple() throws IOException {
		Path directory1 = null, directory2 = null, directory3 = null;
		try {
			directory1 = Files.createTempDirectory("someName1");
			directory2 = Files.createTempDirectory("someName2");
			directory3 = Files.createTempDirectory("someName3");
			ConfigLoader loader = new ConfigLoader();
			loader.setIrisConfigDirPath(directory1.toString() + "," + directory2.toString() + "," + directory3.toString());
			assertEquals(3, loader.getIrisConfigDirPaths().size());
			Iterator<String> it = loader.getIrisConfigDirPaths().iterator();
			assertEquals(directory1.toString(), it.next());
			assertEquals(directory2.toString(), it.next());
			assertEquals(directory3.toString(), it.next());
		} finally {
			Files.deleteIfExists(directory1);
			Files.deleteIfExists(directory2);
			Files.deleteIfExists(directory3);
		}
	}
}

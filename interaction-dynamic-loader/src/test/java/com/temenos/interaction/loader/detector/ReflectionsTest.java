package com.temenos.interaction.loader.detector;

/*
 * #%L
 * interaction-dynamic-loader
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

import com.temenos.interaction.core.command.annotation.InteractionCommandImpl;
import com.temenos.interaction.loader.classloader.ParentLastURLClassloader;
import com.temenos.test.helperclasses.AnnotatedClass1;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

/**
 * The tests verifies if the class already existing on the classpath can be reloaded from a JAR coocked up for the person.
 * It also test scanning for annotations in the prescribed group of JARs only.
 * @author andres
 * @author trojan
 */

public class ReflectionsTest {
    
    @Test
    @Ignore(value = "Not ready yet")
    public void testLoadingClassesFromJar() throws MalformedURLException, ClassNotFoundException {
        File jarFile = new File("src/test/jars/annotations-test-helpers.jar");
       
        ClassLoader classloader = new ParentLastURLClassloader(new URL[]{jarFile.toURI().toURL()});
        Class<?> clz = classloader.loadClass("com.temenos.test.helperclasses.AnnotatedClass1");
        Assert.assertEquals("Annotation name was not read as expected", "test1", clz.getAnnotation(InteractionCommandImpl.class).name());
    }
    
    @Test
    @Ignore(value = "Not ready yet")
    public void testReflectionsOnSpecificPackage() throws MalformedURLException {
        // enforce loading class with current classloader
        AnnotatedClass1 object = new AnnotatedClass1();
        
        File jarFile = new File("src/test/jars/annotations-test-helpers.jar");
       
        ClassLoader classloader = new ParentLastURLClassloader(new URL[]{jarFile.toURI().toURL()}, Thread.currentThread().getContextClassLoader());
        Reflections r = new Reflections(                
                new ConfigurationBuilder()
            .setUrls(jarFile.toURI().toURL())
            .addClassLoader(classloader)
        );
        
        Set<Class<?>> annotated = r.getTypesAnnotatedWith(InteractionCommandImpl.class);
        
        // we knew 3 classes with given annotation was in a jar we prepared
        Assert.assertEquals("The number of classes detected is different than expected",3, annotated.size());
        for (Class cls : annotated) {
            // for every class chack if it was really loaded with the classloader we wanted
            // AnnotatedClass1 - in case of classloading method being faulty would be from parent!
            Assert.assertEquals("Classloader used to load class different than expected, delegation model failed!", cls.getClassLoader(), classloader);
        }
    }
}


package com.temenos.interaction.loader.classloader;
 
/*
* #%L
 * * interaction-dynamic-loader
 * *
 * %%
 * Copyright (C) 2012 - 2015 Temenos Holdings N.V.
 * *
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
 
 
import com.google.common.primitives.Longs;
import com.temenos.interaction.core.loader.FileEvent;
import com.temenos.interaction.loader.objectcreation.ParameterizedFactory;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
 
/**
*
* @author trojanbug
*/
public class CachingParentLastURLClassloaderFactory implements ParameterizedFactory<FileEvent<File>,ClassLoader> {
   
    ClassLoader cache = null;
    Object lastState = null;
 
    @Override
    public synchronized ClassLoader getForObject(FileEvent<File> param) {
       
      Object state = calculateCurrentState(param);
      if (lastState==null || (!lastState.equals(state))) {
          Object previousState = lastState;
          ClassLoader previousCL = cache;
         
          lastState = state;
          cache = createClassLoader(param);
      }
      
      return cache;
    }
 
    protected synchronized ClassLoader createClassLoader(FileEvent<File> param) {
        Set<URL> urls = new HashSet();
       
        Collection<File> files = FileUtils.listFiles(param.getResource(), new String[]{"jar"}, true);
        for (File f : files) {
            try {
                urls.add(f.toURI().toURL());
            } catch (MalformedURLException ex) {
                // should not happen, we do have the file there
                // but if, what can we do - just log it
            }
        }
       
        ClassLoader classloader = new ParentLastURLClassloader(urls.toArray(new URL[]{}), Thread.currentThread().getContextClassLoader());
       
        return classloader;
    }
 
    protected Object calculateCurrentState(FileEvent<File> param) {
        Collection<File> files = FileUtils.listFiles(param.getResource(), new String[]{"jar"}, true);
       
        MessageDigest md = DigestUtils.getMd5Digest();
        for (File f : files) {
           md.update( Longs.toByteArray(f.lastModified()));
        }
       
        return Hex.encodeHexString( md.digest() );
    }
}

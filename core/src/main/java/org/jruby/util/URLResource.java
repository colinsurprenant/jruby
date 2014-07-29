package org.jruby.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import jnr.posix.FileStat;
import jnr.posix.POSIX;

import org.jruby.util.io.ChannelDescriptor;
import org.jruby.util.io.ModeFlags;

public class URLResource implements FileResource {

    public static String URI = "uri:";

    private final String uri;

    private final String[] list;

    private final InputStream is;

    private final JarFileStat fileStat;

    URLResource(String uri, InputStream is, String[] files) {
        this.uri = uri;
        this.list = files;
        this.is = is;
        this.fileStat = new JarFileStat(this);
    }
    
    @Override
    public String absolutePath()
    {
        return uri;
    }

    @Override
    public boolean exists()
    {
        return is != null;
    }

    @Override
    public boolean isDirectory()
    {
        return list != null;
    }

    @Override
    public boolean isFile()
    {
        return list == null;
    }

    @Override
    public long lastModified()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long length()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean canRead()
    {
        return true;
    }

    @Override
    public boolean canWrite()
    {
        return false;
    }

    @Override
    public String[] list()
    {
        return list;
    }

    @Override
    public boolean isSymLink()
    {
        return false;
    }

    @Override
    public FileStat stat(POSIX posix) {
        return fileStat;
    }

    @Override
    public FileStat lstat(POSIX posix) {
      // jars don't have symbolic links, so lstat is no different than regular stat
      return stat(posix);
    }
 
    @Override
    public JRubyFile hackyGetJRubyFile()
    {
        new RuntimeException().printStackTrace();
        return null;
    }

    @Override
    public InputStream getInputStream()
    {
        return is;
    }

    @Override
    public ChannelDescriptor openDescriptor(ModeFlags flags, POSIX posix, int perm)
            throws ResourceException
    {
        return new ChannelDescriptor(getInputStream(), flags);
    }

    public static FileResource create(String pathname)
    {        
        if (!pathname.startsWith( URI )) {
            return null;
        }
        pathname = pathname.substring(URI.length() );
        URL url;
        try
        {
            pathname = pathname.replaceAll("([^:])//", "$1/").replaceFirst( ":/", "://" ).replace("///", "//");
            url = new URL(pathname);
            // we do not want to deal with those url here like this though they are valid url/uri
            if (url.getProtocol().startsWith("http") || url.getProtocol().equals("file")|| url.getProtocol().equals("jar")){
                return null;
            }   
        }
        catch (MalformedURLException e)
        {
            // file does not exists
            return new URLResource(URI + pathname, null, null);
        }
        String[] files = listFiles(pathname);
        if (files != null) {
            return new URLResource(URI + pathname, null, files);
        }
        try
        {   
            return new URLResource(URI + pathname, url.openStream(), null);
        }
        catch (IOException e)
        {
            // can not open stream - treat it as not existing file
            return new URLResource(URI + pathname, null, null);
        }
    }

    public static String[] listFilesFromURL(URL url) {
        BufferedReader reader = null;
        try {
            List<String> files = new LinkedList<String>();
            reader = new BufferedReader( new InputStreamReader( url.openStream() ) );
            String line = reader.readLine();
            while (line != null) {
                files.add( line );
                line = reader.readLine();
            }
            return files.toArray( new String[ files.size() ] );
        }
        catch (IOException e) {
            return null;
        }
        finally {
            if (reader != null) {
                try
                {
                    reader.close();
                }
                catch (IOException ignored)
                {
                }
            }
        }
    }
    
    private static String[] listFiles(String pathname) {
        try
        {
            return listFilesFromURL(new URL(pathname + "/.jrubydir"));
        }
        catch (MalformedURLException e)
        {
            return null;
        }
    }

    public static URL getResourceURL( String location )
    {
        try
        {
            return new URL( location.replaceFirst("^" + URI, ""));
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException("BUG");
        }
    }
}
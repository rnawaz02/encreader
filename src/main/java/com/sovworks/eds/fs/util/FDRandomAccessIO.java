package com.sovworks.eds.fs.util;

import com.sovworks.eds.fs.RandomAccessIO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.core.io.ClassPathResource;

@SuppressWarnings("JniMissingFunction")
public class FDRandomAccessIO implements RandomAccessIO
{
	public FDRandomAccessIO(int fd)
	{
		setFD(fd);
	}

	@Override
	public void close() throws IOException
	{
		if(_fd >= 0)
		{
			close(_fd);
			_fd = -1;
		}
	}

	public int getFD()
	{
		return _fd;
	}

	@Override
	public void seek(long position) throws IOException
	{
		if(_fd < 0)
			throw new IOException("File is closed");
		
		seek(_fd, position);
	}

	@Override
	public long getFilePointer() throws IOException
	{
		if(_fd < 0)
			throw new IOException("File is closed");
		
		return getPosition(_fd);
	}

	@Override
	public long length() throws IOException
	{
		if(_fd < 0)
			throw new IOException("File is closed");
		
		return getSize(_fd);
	}

	@Override
	public synchronized int read() throws IOException
	{
        byte[] buf = new byte[1];
        return (read(buf, 0, 1) != -1) ? buf[0] & 0xff : -1;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		if(off + len > b.length)
			throw new IndexOutOfBoundsException();
		
		if(_fd < 0)
			throw new IOException("File is closed");
		
		int res = read(_fd,b,off,len);
		if(res<0)
			throw new IOException("Failed reading data");
		if(res == 0)
			return -1;
		return res;
	}

	@Override
	public void write(int b) throws IOException
	{
		write(new byte[] {(byte)b}, 0, 1);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		if(off + len > b.length)
			throw new IndexOutOfBoundsException();
		
		if(_fd < 0)
			throw new IOException("File is closed");
		
		if(write(_fd,b,off,len)!=0)
			throw new IOException("Failed writing data");
	}

	@Override
	public void flush() throws IOException
	{	
		if(_fd < 0)
			throw new IOException("File is closed");
		flush(_fd);
	}

	@Override
	public void setLength(long newLength) throws IOException
	{
		if(_fd < 0)
			throw new IOException("File is closed");
		
		if (newLength < 0) 
            throw new IllegalArgumentException("newLength < 0");		
		        
        if(ftruncate(_fd, newLength)!=0)
        	throw new IOException("Failed truncating file");        	

        long filePointer = getFilePointer();
        if (filePointer > newLength) 
            seek(newLength);
	}

    protected FDRandomAccessIO() {}

    protected void setFD(int fd)
    {
        _fd = fd;
    }
	
	static
	{
		//System.loadLibrary("fdraio");
		try {

			InputStream is = new ClassPathResource("libfdraio.so").getInputStream();
			byte[] buffer = new byte[1024];
			int length;
			File templibfdraio = File.createTempFile("libfdraio", ".so");
			OutputStream os = new FileOutputStream(templibfdraio);
			while ((length = is.read(buffer)) != -1) {
				os.write(buffer, 0, length);
			}
			is.close();
			os.close();
			System.load(templibfdraio.getAbsolutePath());
			templibfdraio.deleteOnExit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static native void flush(int fd);
	private static native long getSize(int fd);
	private static native void close(int fd);
	private static native long getPosition(int fd);
	private static native void seek(int fd, long newPosition);
	private static native int ftruncate(int fd, long newLength);	 
	private static native int read(int fd, byte[] buf, int off, int len);
	private static native int write(int fd, byte[] buf, int off, int len);
	
	private int _fd = -1;
}

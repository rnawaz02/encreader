package com.sovworks.eds.crypto.hash;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

import org.springframework.core.io.ClassPathResource;

public class Whirlpool extends MessageDigest
{
	public Whirlpool()
	{
		super("whirlpool");
		_contextPtr = initContext();
		engineReset();
	}	
	
	public void close()
	{
		if(_contextPtr!=0)
		{
			freeContext(_contextPtr);
			_contextPtr = 0;
		}
	}
	
	@Override
	protected void finalize() throws Throwable 
	{
		close();		
	}
	
	@Override
	protected int engineGetDigestLength()
	{
		return DIGEST_LENGTH;
	}	

	@Override
	protected byte[] engineDigest()
	{
		byte[] res = new byte[DIGEST_LENGTH];
		finishDigest(_contextPtr, res);
		engineReset();
		return res;
	}

	@Override
	protected void engineReset()
	{
		resetDigest(_contextPtr);
	}

	@Override
	protected void engineUpdate(byte input)
	{
		updateDigestByte(_contextPtr, input);		
	}

	@Override
	protected void engineUpdate(byte[] input, int offset, int len)
	{
		updateDigest(_contextPtr, input, offset, len);		
	}
	
	static
	{
		//System.loadLibrary("edswhirlpool");
		try {	
			InputStream is = new ClassPathResource("libedswhirlpool.so").getInputStream();	
			byte[] buffer = new byte[1024];
			int length;
			File templibedswhirlpool = File.createTempFile("libedswhirlpool", ".so");
			OutputStream os = new FileOutputStream(templibedswhirlpool);	
			while ((length = is.read(buffer)) != -1) {
				os.write(buffer, 0, length);
			}
			is.close();
			os.close();			
			System.load(templibedswhirlpool.getAbsolutePath());
			templibedswhirlpool.deleteOnExit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static final int DIGEST_LENGTH = 64;
	
	private long _contextPtr;
	
	private native long initContext();
	private native void freeContext(long contextPtr);
	private native void resetDigest(long contextPtr);
	private native void updateDigestByte(long contextPtr,byte data);
	private native void updateDigest(long contextPtr,byte[] data,int offset,int len);
	private native void finishDigest(long contextPtr,byte[] result);

	
	
	
}
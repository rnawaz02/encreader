package com.sovworks.eds.crypto.blockciphers;

import com.sovworks.eds.crypto.EncryptionEngineException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.core.io.ClassPathResource;

import com.sovworks.eds.crypto.BlockCipherNative;


public class Serpent implements BlockCipherNative
{	
	@Override
	public void init(byte[] key) throws EncryptionEngineException
	{
		_contextPtr = initContext(key);
		if(_contextPtr == 0)
			throw new EncryptionEngineException("Serpent context initialization failed");
		
	}
	@Override
	public void encryptBlock(byte[] data) throws EncryptionEngineException
	{
		if(_contextPtr==0)
			throw new EncryptionEngineException("Cipher is closed");
		encrypt(data,_contextPtr);
		
	}
	@Override
	public void decryptBlock(byte[] data) throws EncryptionEngineException
	{
		if(_contextPtr==0)
			throw new EncryptionEngineException("Cipher is closed");
		decrypt(data,_contextPtr);		
	}
	
	@Override
	public void close()
	{
		if(_contextPtr!=0)
		{
			closeContext(_contextPtr);
			_contextPtr = 0;
		}		
	}
	
	@Override
	public long getNativeInterfacePointer() throws EncryptionEngineException
	{
		return _contextPtr;
	}
	
	@Override
	public int getKeySize()
	{
		return 32;
	}
	
	@Override
	public int getBlockSize()
	{
		return 16;
	}
	
	static
	{
		//System.loadLibrary("edsserpent");
		try {

			InputStream is = new ClassPathResource("libedsserpent.so").getInputStream();
			byte[] buffer = new byte[1024];
			int length;
			File templibedsserpent = File.createTempFile("libedsserpent", ".so");
			OutputStream os = new FileOutputStream(templibedsserpent);
			while ((length = is.read(buffer)) != -1) {
				os.write(buffer, 0, length);
			}
			is.close();
			os.close();
			System.load(templibedsserpent.getAbsolutePath());
			templibedsserpent.deleteOnExit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private long _contextPtr;
	
	private native long initContext(byte[] key);	
	private native void closeContext(long contextPtr);
	private native void encrypt(byte[] data,long contextPtr);
	private native void decrypt(byte[] data,long contextPtr);
}

    
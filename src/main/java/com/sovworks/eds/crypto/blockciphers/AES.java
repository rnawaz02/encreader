package com.sovworks.eds.crypto.blockciphers;

import com.sovworks.eds.crypto.EncryptionEngineException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.core.io.ClassPathResource;

import com.sovworks.eds.crypto.BlockCipherNative;

public class AES implements BlockCipherNative {
	public AES() {
		this(32);
	}

	public AES(int keySize) {
		_keySize = keySize;
	}

	@Override
	public void init(byte[] key) throws EncryptionEngineException {
		if (key.length != getKeySize())
			throw new IllegalArgumentException(
					String.format("Wrong key length. Required: %d. Provided: %d", _keySize, key.length));
		_contextPtr = initContext(key);
		if (_contextPtr == 0)
			throw new EncryptionEngineException("AES context initialization failed");

	}

	@Override
	public void encryptBlock(byte[] data) throws EncryptionEngineException {
		if (_contextPtr == 0)
			throw new EncryptionEngineException("Cipher is closed");
		encrypt(data, _contextPtr);

	}

	@Override
	public void decryptBlock(byte[] data) throws EncryptionEngineException {
		if (_contextPtr == 0)
			throw new EncryptionEngineException("Cipher is closed");
		decrypt(data, _contextPtr);
	}

	@Override
	public void close() {
		if (_contextPtr != 0) {
			closeContext(_contextPtr);
			_contextPtr = 0;
		}
	}

	@Override
	public long getNativeInterfacePointer() throws EncryptionEngineException {
		return _contextPtr;
	}

	@Override
	public int getKeySize() {
		return _keySize;
	}

	@Override
	public int getBlockSize() {
		return 16;
	}

	static {
		// System.loadLibrary("edsaes");
		try {

			InputStream is = new ClassPathResource("libedsaes.so").getInputStream();
			byte[] buffer = new byte[1024];
			int length;
			File templibedsaes = File.createTempFile("libedsaes", ".so");
			OutputStream os = new FileOutputStream(templibedsaes);
			while ((length = is.read(buffer)) != -1) {
				os.write(buffer, 0, length);
			}
			is.close();
			os.close();
			System.load(templibedsaes.getAbsolutePath());
			templibedsaes.deleteOnExit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private long _contextPtr;
	private int _keySize;

	private native long initContext(byte[] key);

	private native void closeContext(long contextPtr);

	private native void encrypt(byte[] data, long contextPtr);

	private native void decrypt(byte[] data, long contextPtr);
}

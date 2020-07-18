package com.tak;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ResourceUtils;

import com.sovworks.eds.fs.RandomAccessIO;
import com.sovworks.eds.fs.errors.FileInUseException;
import com.sovworks.eds.fs.fat.FatFS;
import com.sovworks.eds.fs.fat.FatFS.FatDirectory;
import com.sovworks.eds.fs.fat.FatFS.FatPath;
import com.sovworks.eds.fs.fat.DirEntry;

import com.sovworks.eds.container.ContainerFormatInfo;
import com.sovworks.eds.container.EdsContainer;
import com.sovworks.eds.container.VolumeLayout;
import com.sovworks.eds.container.VolumeLayoutBase;
import com.sovworks.eds.crypto.FileEncryptionEngine;
import com.sovworks.eds.exceptions.ApplicationException;
import com.sovworks.eds.fs.Directory.Contents;
import com.sovworks.eds.fs.File.AccessMode;
import com.sovworks.eds.fs.FileSystem;
import com.sovworks.eds.fs.std.StdFs;
import com.sovworks.eds.fs.std.StdFsFileIO;
//import com.sovworks.eds.fs.util.ContainerFSWrapper;
import com.sovworks.eds.fs.util.PathUtil;
import com.sovworks.eds.truecrypt.FormatInfo;
import com.sovworks.eds.truecrypt.StdLayout;

import com.sovworks.eds.fs.Path;

@SpringBootApplication
public class Demo1Application {

public static void main(String[] args) {

		SpringApplication.run(Demo1Application.class, args);
		
		
		System.out.println("workin sg 123 check");
		
		
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

		
		

	/*
		Resource  resource = Demo1Application.appContext.getResource("classpath:libedsaes.so");  
		byte[] buffer = new byte[1024];
		int length;
		try {
			
			File file = resource.getFile(); //ResourceUtils.getFile("classpath:libedsaes.so");
			InputStream is = new FileInputStream(file);
				
			File templibedsaes = File.createTempFile("libedsaes", ".so");
			OutputStream os = new FileOutputStream(templibedsaes);	
			while ((length = is.read(buffer)) != -1) {
				os.write(buffer, 0, length);
			}
			System.load(templibedsaes.getAbsolutePath());
			templibedsaes.deleteOnExit();

			is.close();
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// public StdFsFileIO(File f,AccessMode mode) throws IOException

		RandomAccessIO io;

		StdFs stdFs = StdFs.getStdFs();

		EdsContainer cnt; // = null;

		
		try {

			StdLayout stdl = new StdLayout();
			stdl.initNew();
			io = new StdFsFileIO(new File("/home/rab/shared/tcontainers/con1"), AccessMode.Read);
			if (stdl.readHeader(io)) {
				System.out.println("Read header successfully!");
			} else {
				System.out.println("Read header error!!");
			}
			io.close();

			FileSystem _fileSystem = null;
			if (_fileSystem == null) {
				System.out.println("--- start proto ---");
				// cnt = new
				// EdsContainer(stdFs.getPath("/home/rab/shared/econtainers/con1.eds"));
				cnt = new EdsContainer(stdFs.getPath("/home/rab/shared/tcontainers/con3"));

				cnt.setContainerFormat(null);
				cnt.setEncryptionEngineHint(null);
				cnt.setHashFuncHint(null);
				cnt.setNumKDFIterations(0);
				ContainerFormatInfo cfi = new FormatInfo(); // getContainerFormatInfo();

				if (cfi != null) {
					cnt.setContainerFormat(cfi);
					VolumeLayout vl = cfi.getVolumeLayout();
					String name = "aes-xts-plain64"; // getExternalSettings().getEncEngineName();

					if (name != null && !name.isEmpty())
						cnt.setEncryptionEngineHint((FileEncryptionEngine) VolumeLayoutBase
								.findEncEngineByName(vl.getSupportedEncryptionEngines(), name));

					name = "SHA-512"; // getExternalSettings().getHashFuncName();

					if (name != null && !name.isEmpty())
						cnt.setHashFuncHint(VolumeLayoutBase.findHashFunc(vl.getSupportedHashFuncs(), name));
				}

				int numKDFIterations = 0; // getSelectedKDFIterations();
				if (numKDFIterations > 0)
					cnt.setNumKDFIterations(numKDFIterations);
				byte[] pass = "welcome1".getBytes(); // getFinalPassword();

				try {
					cnt.open(pass);
					RandomAccessIO eio = cnt.getEncryptedFile(true);
					FatFS ffs = FatFS.getFat(eio);
					Object opTag = new Object();
					Path fp = ffs.getPath("/test1");
					FatFS.FatDirectory fd = ffs.new FatDirectory((FatPath) fp);

					System.out.println(fd.getName());
					System.out.println(fd.getTotalSpace());
					System.out.println(fd.getCreateDate());

					Contents dirReader = fd.list();

				
					for (Path p : dirReader) {
						FatFS.FatDirectory fdtemp = ffs.new FatDirectory((FatPath) p);
						System.out.print(fdtemp.getName() + "\t");
						if (p.isDirectory()) {
							System.out.print("DIRETORY\t");
						} else {
							System.out.print("FILE\t");

							if (fdtemp.getName().equals("error_log_tak--off")) {

								System.out.println();

								InputStream in = p.getFile().getInputStream();

								InputStreamReader isReader = new InputStreamReader(in);
								// Creating a BufferedReader object
								BufferedReader reader = new BufferedReader(isReader);
								// StringBuffer sb = new StringBuffer();
								String str;
								while ((str = reader.readLine()) != null) {
									// sb.append(str);
									System.out.println(str);
								}

								// System.out.print("\n"+ sb.toString() +"\n");

							}
						}
						System.out.print(fd.getTotalSpace() + "\t");
						SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-d");
						SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
						System.out.print(date.format(fd.getCreateDate()) + "\t");
						System.out.print(time.format(fd.getCreateDate()) + "\n");
					}

					System.out.println("--- end proto ---");
				} catch (FileInUseException e) {
					System.out.println(e);
				}
				System.out.println("check");
			}

			// FileSystem fs = getEncryptedFS(true);

			// return fs;

		} catch (IOException | ApplicationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	
		try {
			// cw.open();
			System.out.println("\nworking");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	*/
	}
}

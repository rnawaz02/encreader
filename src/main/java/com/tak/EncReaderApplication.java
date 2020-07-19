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
import java.nio.file.Paths;
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
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;

import com.sovworks.eds.fs.Path;
import org.apache.commons.cli.*;
@SpringBootApplication
public class EncReaderApplication implements CommandLineRunner {

	public static void main(String[] args) {

		try {
			SpringApplication app = new SpringApplication(EncReaderApplication.class);
			app.setBannerMode(Banner.Mode.OFF);
			app.run(args);
		}catch(Exception e) {
		}
	}

	@Override
	public void run(String[] args) {
				
		Options options = new Options();
		
        Option c = new Option("f", "file", true, "absolute container path on the file system");
        c.setRequired(true);
        options.addOption(c);

        Option pw = new Option("pw", "password", true, "password");
        pw.setRequired(true);
        options.addOption(pw);
        
        Option openfile = new Option("of", "openfile", true, "absolute file path inside container");
        openfile.setRequired(false);
        options.addOption(openfile);
        
        Option opendir = new Option("od", "opendir", true, "absolute directory path inside container");
        opendir.setRequired(false);
        options.addOption(opendir);
        
        Option help = new Option("h", "help", false, "Help");
        help.setRequired(false);
        options.addOption(help);
        CommandLineParser parser = new ExtendedParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;
     
        try {
        	 cmd = parser.parse(options, args, false);
        	 	if(cmd.hasOption("help")) {
        	        formatter.printHelp("Encryption Utility", options);
                    System.exit(1);
        	 	}
        	    String container = cmd.getOptionValue("file");
                String password = cmd.getOptionValue("password");
                String of = cmd.getOptionValue("openfile");
                String od = cmd.getOptionValue("opendir");
               
                if(of != null) {
                	//System.out.println("open file");
            		this.doWork(container, password.getBytes(), of, "openfile");
                }else if(od != null) {
                	//System.out.println("open directory");
              		this.doWork(container, password.getBytes(), od, "opendir");
                } else if(of == null && od == null) {
                	//System.out.println("open root directory");
              		this.doWork(container, password.getBytes(), "/", "opendir");
                }
                
                
                //this.doWork("/home/rab/shared/containers/con512", "welcome1".getBytes(), "/", "opendir");
         } catch (org.apache.commons.cli.ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("Encryption Utility", options);
            System.exit(1);
        }
	}

	private void doWork(String container, byte[] password, String target, String action) {

		RandomAccessIO io;
		StdFs stdFs = StdFs.getStdFs();
		EdsContainer cnt; // = null;
		try {
			StdLayout stdl = new StdLayout();
			stdl.setPassword(password);
			stdl.initNew();
			io = new StdFsFileIO(new File(container), AccessMode.Read);
			if (!stdl.readHeader(io, password)) {
				System.out.println("Cannot encrypt container " + container);
				io.close();		
				System.exit(1);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
			
		try {			
			FileSystem _fileSystem = null;
			if (_fileSystem == null) {
				cnt = new EdsContainer(stdFs.getPath(container));
				cnt.setContainerFormat(null);
				cnt.setEncryptionEngineHint(null);
				cnt.setHashFuncHint(null);
				cnt.setNumKDFIterations(0);
				ContainerFormatInfo cfi = new FormatInfo(); // getContainerFormatInfo();

				if (cfi != null) {
					cnt.setContainerFormat(cfi);
					VolumeLayout vl = cfi.getVolumeLayout();
					String name = ""; //"aes-xts-plain64"; // getExternalSettings().getEncEngineName();

					if (name != null && !name.isEmpty())
						cnt.setEncryptionEngineHint((FileEncryptionEngine) VolumeLayoutBase
								.findEncEngineByName(vl.getSupportedEncryptionEngines(), name));
					name = ""; //"SHA-512"; // getExternalSettings().getHashFuncName();

					if (name != null && !name.isEmpty())
						cnt.setHashFuncHint(VolumeLayoutBase.findHashFunc(vl.getSupportedHashFuncs(), name));
				}

				int numKDFIterations = 0; // getSelectedKDFIterations();
				if (numKDFIterations > 0)
					cnt.setNumKDFIterations(numKDFIterations);
				byte[] pass = password; // getFinalPassword();
				try {
					cnt.open(pass);
					RandomAccessIO eio = cnt.getEncryptedFile(true);
					FatFS ffs = FatFS.getFat(eio);
					Object opTag = new Object();
					Path fp;
					if(action.equals("opendir")) {
						fp = ffs.getPath(target);				
					}else {
						//System.out.println(Paths.get(target).getParent().toString());
						fp = ffs.getPath(Paths.get(target).getParent().toString());										
					}
					FatFS.FatDirectory fd = ffs.new FatDirectory((FatPath) fp);

					//System.out.println(fd.getName());
					//System.out.println(fd.getTotalSpace());
					//System.out.println(fd.getCreateDate());

					Contents dirReader = fd.list();
					
					if(action.equals("opendir")) {
						for (Path p : dirReader) {
							FatFS.FatDirectory fdtemp = ffs.new FatDirectory((FatPath) p);
							System.out.print(fdtemp.getName() + "\t");
							//System.out.print(p.getFile().getName() + "\t");
							if (p.isDirectory()) {
								System.out.print("DIRETORY\t");
							} else {
								System.out.print("FILE\t");
							}
							System.out.print(fd.getTotalSpace() + "\t");
							SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-d");
							SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
							System.out.print(date.format(fd.getCreateDate()) + "\t");
							System.out.print(time.format(fd.getCreateDate()) + "\n");
						}	
						
					}else {
						
						for (Path p : dirReader) {
							FatFS.FatDirectory fdtemp = ffs.new FatDirectory((FatPath) p);
							//System.out.print(fdtemp.getName() + "\t");
							//System.out.print(p.getFile().getName() + "\t");
							
							if (p.isFile()) {
								if (fdtemp.getName().equals(Paths.get(target).getFileName().toString())) {

				//					System.out.println();

									InputStream in = p.getFile().getInputStream();
									InputStreamReader isReader = new InputStreamReader(in);
									// Creating a BufferedReader object
									BufferedReader reader = new BufferedReader(isReader);
									StringBuffer sb = new StringBuffer();
									String str;
									while ((str = reader.readLine()) != null) {
										//sb.append(str);
										System.out.println(str);
									}

									// System.out.print("\n"+ sb.toString() +"\n");
									break;
								}
							}
						}		
					}
				} catch (FileInUseException e) {
					System.out.println(e);
				}
			}
		} catch (IOException | ApplicationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}

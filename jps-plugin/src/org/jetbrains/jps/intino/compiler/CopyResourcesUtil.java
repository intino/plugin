package org.jetbrains.jps.intino.compiler;

import java.io.*;
import java.nio.file.FileSystemException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CopyResourcesUtil {
	private static final Logger LOG = Logger.getLogger(CopyResourcesUtil.class.getName());


	public static boolean copy(File source, File target) {
		if (!source.isDirectory())
			return copyFile(source.getAbsolutePath(), new File(target, source.getName()).getAbsolutePath());
		try {
			return copyDir(source, target);
		} catch (FileSystemException e) {
			return false;
		}
	}


	public static Boolean copyDir(String sSource, String sDestination) throws FileSystemException {
		File oSource = new File(sSource);
		File oDestination = new File(sDestination);
		return copyDir(oSource, oDestination);
	}

	public static Boolean copyDir(File oSource, File oDestination) throws FileSystemException {
		try {
			if (oSource.exists()) {
				if (oSource.isDirectory()) {
					if (!oDestination.exists())
						oDestination.mkdir();
					String[] children = oSource.list();
					for (String aChildren : children)
						copyDir(new File(oSource, aChildren), new File(oDestination, aChildren));
				} else {
					InputStream in = new FileInputStream(oSource);
					OutputStream out = new FileOutputStream(oDestination);
					byte[] buf = new byte[1024];
					int len;
					while ((len = in.read(buf)) > 0)
						out.write(buf, 0, len);
					in.close();
					out.close();
				}
				return true;
			}
		} catch (IOException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
			throw new FileSystemException(e.getMessage(), oSource.getName(), e.getMessage());
		}
		return false;
	}

	public static Boolean forceDir(String sDirname) {
		return new File(sDirname).mkdirs();
	}

	public static Boolean createFile(String sFilename) {
		try {
			new File(sFilename).createNewFile();
		} catch (IOException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
			return false;
		}
		return true;
	}

	public static Boolean copyFile(String source, String destination) {
		try {
			return copyFile(new FileInputStream(new File(source)), new File(destination));
		} catch (FileNotFoundException | FileSystemException e) {
			LOG.log(Level.SEVERE, "Could not copy the file: " + source + "\n" + e.getMessage(), e);
			return false;
		}
	}

	public static Boolean copyFile(InputStream source, File destination) throws FileSystemException {
		forceDir(destination.getParentFile().getAbsolutePath());
		try {
			OutputStream out = new FileOutputStream(destination);
			byte[] buf = new byte[1024];
			int len;
			while ((len = source.read(buf)) > 0)
				out.write(buf, 0, len);
			source.close();
			out.close();
		} catch (IOException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
			throw new FileSystemException("Could not copy the file");
		}

		return true;
	}
}

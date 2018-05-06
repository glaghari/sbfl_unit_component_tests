package org.lore.compress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
/**
 * Taken from Stackoverflow
 *
 */
public class ZipUtils
{

private List<String> fileList;
private String OUTPUT_ZIP_FILE;
private String SOURCE_FOLDER; // SourceFolder path

public ZipUtils(String OUTPUT_ZIP_FILE, String SOURCE_FOLDER)
{
	this.OUTPUT_ZIP_FILE = OUTPUT_ZIP_FILE;
	this.SOURCE_FOLDER = SOURCE_FOLDER;
	this.fileList = new ArrayList<String>();
}

public void zip()
{
   generateFileList(new File(SOURCE_FOLDER));
   zipIt(OUTPUT_ZIP_FILE);
}

public void zipIt(String zipFile)
{
   byte[] buffer = new byte[1024];
   String source = "";
   FileOutputStream fos = null;
   ZipOutputStream zos = null;
   try
   {
      try
      {
         source = SOURCE_FOLDER.substring(SOURCE_FOLDER.lastIndexOf(File.separator) + 1, SOURCE_FOLDER.length());
      }
     catch (Exception e)
     {
        source = SOURCE_FOLDER;
     }
     fos = new FileOutputStream(zipFile);
     zos = new ZipOutputStream(fos);

     FileInputStream in = null;

     for (String file : this.fileList)
     {
        ZipEntry ze = new ZipEntry(source + File.separator + file);
        zos.putNextEntry(ze);
        try
        {
           in = new FileInputStream(SOURCE_FOLDER + File.separator + file);
           int len;
           while ((len = in.read(buffer)) > 0)
           {
              zos.write(buffer, 0, len);
           }
        }
        finally
        {
           in.close();
        }
     }

     zos.closeEntry();
  }
  catch (IOException ex)
  {
     ex.printStackTrace();
  }
  finally
  {
     try
     {
        zos.close();
     }
     catch (IOException e)
     {
        e.printStackTrace();
     }
  }
}

public void generateFileList(File node)
{
  // add file only
  if (node.isFile())
  {
		  fileList.add(generateZipEntry(node.toString()));

  }

  if (node.isDirectory())
  {
     File[] subNote = node.listFiles();
     for (File filename : subNote)
     {
        generateFileList(filename);
     }
  }
}

private String generateZipEntry(String file)
{
   return file.substring(SOURCE_FOLDER.length() + 1, file.length());
}
}  
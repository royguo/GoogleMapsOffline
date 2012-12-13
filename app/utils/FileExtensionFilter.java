package utils;

import java.io.File;
import java.io.FileFilter;

public class FileExtensionFilter implements FileFilter {

  public boolean accept(File pathname) {
    String filename = pathname.getName();
    if (filename.lastIndexOf(".idx") > -1) {
      return true;
    } else
      return false;
  }

}

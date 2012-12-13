package utils;

import play.Play;

public class Constants {
  public final static String PANO_DIR() {
    String separator = Play.applicationPath.separator;
    StringBuilder builder = new StringBuilder(Play.applicationPath.getAbsolutePath() + separator);
    builder.append("data" + separator);
    return builder.toString();
  }
}

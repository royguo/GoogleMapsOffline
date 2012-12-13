package utils;

import play.Play;

public class Utils {

  public static String getApplicationPath(String... dirs) {
    String separator = Play.applicationPath.separator;
    StringBuilder builder = new StringBuilder(Play.applicationPath.getAbsolutePath() + separator);
    for (String s : dirs) {
      builder.append(s + separator);
    }
    return builder.toString();
  }

}

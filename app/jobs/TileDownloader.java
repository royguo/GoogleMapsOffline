package jobs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

import play.Logger;
import play.jobs.Job;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import utils.Utils;



public class TileDownloader extends Job {
  public int x;
  public int y;
  public int zoom;
  public int zoomMax;
  public int count = 0;

  public TileDownloader(int x, int y, int zoom, int zoomMax) {
    this.x = x;
    this.y = y;
    this.zoom = zoom;
    this.zoomMax = zoomMax;
  }

  @Override
  public void doJob() {
    Logger.info("begin to download Google Maps Tile");
    recursionDownload(x, y, zoom);
    Logger.info("finish downloading Google Maps Tile");
  }

  /**
   * 269019 389774 269018 389773 20
   * <p>
   * 538038 779548 538036 779546 21 /
   */
  public void recursionDownload(int x, int y, int zoom) {
    if (zoom > this.zoomMax) return;
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        recursionDownload(x * 2 + i, y * 2 + j, zoom + 1);
      }
    }
    // download current image
    downloadImage(x, y, zoom);
    Logger.info("Image downloading,  count = " + ++count + ",  zoom = " + zoom + " , x = " + x
        + ", y = " + y);
  }

  public void downloadImage(int x, int y, int zoom) {
    String url =
        "http://mt1.googleapis.com/vt?lyrs=m@174000000&src=apiv3&hl=zh-CN&x=" + x + "&s=&y=" + y
            + "&z=" + zoom + "&s=Gali&style=api%7Csmartmaps";
    String path = Utils.getApplicationPath("public", "maps", "expotile", zoom + "", x + "");
    File dir = new File(path);
    if (!dir.exists()) {
      dir.mkdirs();
    }
    File image = new File(path + y + ".png");
    try {
      if (!image.exists() || ImageIO.read(image) == null) {
        HttpResponse response = WS.url(url).get();
        InputStream is = response.getStream();
        FileOutputStream fos = new FileOutputStream(image);
        byte[] bytes = new byte[1024];
        while (is.read(bytes) > -1) {
          fos.write(bytes);
        }
        is.close();
        fos.flush();
        fos.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

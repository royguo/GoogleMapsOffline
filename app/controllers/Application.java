package controllers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.List;

import jobs.TileDownloader;
import models.Marker;
import play.Logger;
import play.cache.Cache;
import play.libs.Codec;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.mvc.Controller;
import utils.Constants;
import utils.FileExtensionFilter;
import utils.Utils;

/**
 * Image render、Maps cache etc.
 */
public class Application extends Controller {

    public static void index() {
        render();
    }

    public static void cacheMaps() {
        render();
    }

    /**
     * <image id> <lat> <lng> <offset in data file> <image length>
     */
    public static void image(String fileName, int offset, int length) throws Exception {
        String cacheKey = Codec.hexMD5(fileName + offset + length);
        byte[] bytes = null;
        if (Cache.get(cacheKey) != null) {
            bytes = (byte[]) Cache.get(cacheKey);
        } else {
            // Load image from data set.
            File images = new File(Constants.PANO_DIR() + fileName);
            RandomAccessFile randomAccessFile = new RandomAccessFile(images, "r");
            bytes = new byte[length];
            randomAccessFile.seek(offset);
            randomAccessFile.read(bytes, 0, length);
            randomAccessFile.close();
            Cache.add(cacheKey, bytes, "10mn");
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        // render the image
        response.setContentTypeIfNotSet("IMAGE/JPEG");
        renderBinary(bis, length);
    }



    public static void imageRender() throws Exception {
        File bigFile = new File(Constants.PANO_DIR() + "data_000");
        FileInputStream fis = new FileInputStream(bigFile);
        response.setContentTypeIfNotSet("IMAGE/JPEG");
        renderBinary(fis);
    }

    /**
     * initial google map markers.
     * @throws Exception
     */
    public static void firstMarker() throws Exception {
        File dir = new File(Constants.PANO_DIR());
        FileExtensionFilter filter = new FileExtensionFilter();
        File[] files = dir.listFiles(filter);
        StringBuilder builder = new StringBuilder();
        if (files.length >= 1) {
            RandomAccessFile accessFile = new RandomAccessFile(files[0], "r");
            String line = accessFile.readLine();
            if (line != null) {
                String[] segs = line.split(" ");
                builder.append("{\"x\":\"" + segs[1] + "\",");
                builder.append("\"y\":\"" + segs[2] + "\",");
                builder.append("\"offset\":\"" + segs[3] + "\",");
                builder.append("\"length\":\"" + segs[4] + "\",");
                builder.append("\"file\":\"" + files[0].getName().replace(".idx", "") + "\"}");
            }
        }
        renderJSON(builder.toString());
    }

    public static void markerWithBounds(float latMin, float latMax, float lagMin, float lagMax) {
        StringBuilder builder = new StringBuilder("[");
        List<Marker> markers =
                Marker.find("lat >= ? and lat <= ? and lag >= ? and lag <= ?", latMin, latMax,
                        lagMin, lagMax).fetch();
        for (Marker marker : markers) {
            builder.append("{\"x\":\"" + marker.lat + "\",");
            builder.append("\"y\":\"" + marker.lag + "\",");
            builder.append("\"offset\":\"" + marker.offset + "\",");
            builder.append("\"length\":\"" + marker.length + "\",");
            builder.append("\"file\":\"" + marker.fileName + "\"},");
        }
        String json = builder.toString().replaceAll("\\},$", "\\}\\]");
        if (json.equals("[")) json = json + "]";
        renderJSON(json);
    }

    public static void cacheImage(String x, String y, String zoom) {
        String url =
                "http://mt1.googleapis.com/vt?lyrs=m@174000000&src=apiv3&hl=zh-CN&x=" + x
                        + "&s=&y=" + y + "&z=" + zoom + "&s=Gali&style=api%7Csmartmaps";
        String path = Utils.getApplicationPath("public", "maps", "expotile", zoom, x);
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File image = new File(path + y + ".png");
        if (!image.exists()) {
            try {
                HttpResponse response = WS.url(url).get();
                InputStream is = response.getStream();
                FileOutputStream fos = new FileOutputStream(image);
                byte[] bytes = new byte[1024];
                while (is.read(bytes) > -1) {
                    fos.write(bytes);
                }
                Logger.info("image downloaded : " + url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 递归遍历某一个区域的地图图片
     * @param x
     * @param y
     * @param zoom
     */
    public static void cacheTile(int x, int y, int zoom, int zoomMax) {
        if (x > 0 && y > 0 && zoomMax > 0) {
            new TileDownloader(x, y, zoom, zoomMax).now();
        }
    }
}

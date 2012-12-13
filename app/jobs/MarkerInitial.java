package jobs;

import java.io.File;
import java.io.RandomAccessFile;

import models.Marker;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import utils.Constants;
import utils.FileExtensionFilter;

/**
 * Index all the idx files. Not work on GAE.
 */
@OnApplicationStart
public class MarkerInitial extends Job {

    @Override
    public void doJob() throws Exception {
        File dir = new File(Constants.PANO_DIR());
        FileExtensionFilter filter = new FileExtensionFilter();
        File[] files = dir.listFiles(filter);
        for (File file : files) {
            RandomAccessFile accessFile = new RandomAccessFile(file, "r");
            String line = accessFile.readLine();
            Marker m =
                    Marker.find("lat = ? and lag = ?", Float.parseFloat(line.split(" ")[1]),
                            Float.parseFloat(line.split(" ")[2])).first();
            if (m != null) {
                continue;
            }
            while (line != null) {
                new Marker(line, file.getName()).save();
                line = accessFile.readLine();
            }
        }
    }
}

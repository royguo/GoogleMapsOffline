package models;

import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
@Table(name = "marker")
public class Marker extends Model {
  public String fileName;// fileName with no extension.
  public float lat; // 纬度
  public float lag; // 经度
  public long offset;
  public long length;

  public Marker() {
  }

  public Marker(String line, String fileName) {
    this.fileName = fileName.replace(".idx", "");
    String[] segs = line.split(" ");
    if (segs.length == 5) {
      this.lat = Float.parseFloat(segs[1]);
      this.lag = Float.parseFloat(segs[2]);
      this.offset = Long.parseLong(segs[3]);
      this.length = Long.parseLong(segs[4]);
    }
  }
}

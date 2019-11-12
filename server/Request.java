import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Request implements Serializable{
  public static final long serialVersionUID = 1190299301293857102L;
  public String source, filter, technology;

  public byte[] byteArray;

  //Constructor with 3 params
  public Request(String source, String filter, String technology) throws Exception{
    this.source = source;
    this.filter = filter;
    this.technology = technology;

    //begin img preparation into byte array
    BufferedImage img = ImageIO.read(new File(source));
    ByteArrayOutputStream bstream = new ByteArrayOutputStream();
    ImageIO.write(img,"png",bstream);
    //end img preparation into byte array


    this.byteArray = bstream.toByteArray();
  }

  public String getFileName(){
    File f = new File(source);
    String name =  f.getName();

    return name;
  }

  public String getFilter(){
    return filter;
  }

  public String getTechnology(){
    return technology;
  }

  public byte[] getByteArray(){
    return byteArray;
  }
}

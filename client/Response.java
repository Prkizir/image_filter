import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Response implements Serializable{
  public static final long serialVersionUID = 2182949102003818271L;
  public String source;

  public byte[] byteArray;

  public Response(String source) throws Exception{
    this.source = source;

    BufferedImage img = ImageIO.read(new File(source));
    ByteArrayOutputStream bstream = new ByteArrayOutputStream();
    ImageIO.write(img, "png", bstream);

    this.byteArray = bstream.toByteArray();
  }

  public String getFileName(){
    File f = new File(source);
    String name = f.getName();

    return name;
  }

  public byte[] getByteArray(){
    return byteArray;
  }
}

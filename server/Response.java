/*
*  Response <Server> source code: Object class to send through socket after
*     image has been processed. Includes image with new file name.
*  Copyright (C) 2019  Sergio Isaac Mercado Silvano
*
*  This program is free software: you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  You should have received a copy of the GNU General Public License
*  along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

//Required libraries

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

  //Constructor

  public Response(String source) throws Exception{
    this.source = source;

    //Prepare image into byte array
    BufferedImage img = ImageIO.read(new File(source));
    ByteArrayOutputStream bstream = new ByteArrayOutputStream();
    ImageIO.write(img, "png", bstream);

    this.byteArray = bstream.toByteArray();
  }

  //Obtain filename

  public String getFileName(){
    File f = new File(source);
    String name = f.getName();

    return name;
  }

  //Obtain image byte array

  public byte[] getByteArray(){
    return byteArray;
  }
}

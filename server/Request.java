/*
*  Request <Server> source code: object class to receive object through a socket. Includes
*       filter parameters and image to process in the server side.
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

  //Constructor

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

  //Obtain filename from file

  public String getFileName(){
    File f = new File(source);
    String name =  f.getName();

    return name;
  }

  //Obtain filter to apply

  public String getFilter(){
    return filter;
  }

  //Obtain technology to apply the filter with

  public String getTechnology(){
    return technology;
  }

  //Obtain image byte array

  public byte[] getByteArray(){
    return byteArray;
  }
}

/*
*  Client source code: implements the cmd interface for the user to send a Request
*       to the Server side.
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

//Required Libraries

import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;


//Client

public class Client{
  public static void main(String[] args){

    //Params

    String source, destination, filter, technology;

    //Program Introduction

    System.out.printf("Welcome:\n");
    System.out.printf("BGE Filter Copyright (C) 2019 Sergio Isaac Mercado Silvano\n");
    System.out.printf("This program comes with ABSOLUTELY NO WARRANTY;\n");
    System.out.printf("This is free software, and you are welcome to redistribute it under certain conditions;\n");

    System.out.printf("\nusage: [image_source_path] [filter] [image_destination_path] [technology]\n");
    System.out.printf("                            <blur>                             <java>\n");
    System.out.printf("                            <gray>                             <openmp> <-- NOT YET IMPLEMENTED\n");
    System.out.printf("                            <edge>                             <cuda> <-- NOT YET IMPLEMENTED\n");
    System.out.printf("                                                               <tbb> <-- NOT YET IMPLEMENTED\n\n");

    System.out.printf("Type: \"exit\" to quit the program.\n\n");

    String host = "10.25.24.6"; //GPU Server address
    int port = 8991;  //Working port on host

    //Connection to server via socket

    try(Socket socket = new Socket(host,port)){
      OutputStream os = socket.getOutputStream();
      InputStream is = socket.getInputStream();

      Scanner scanner = new Scanner(System.in);
      String str = null;

      //Client program: continously running until exit condition

      while(true){

        System.out.printf("> \n");
        str = scanner.nextLine();

        //Check if user input is "exit"; close connection if true

        if(str.equals("exit")){
          System.out.printf("Closing the connection...\n");
          socket.close();
          System.out.printf("Connection closed\n");
          break;
        }

        //Prepare object IO streams

        ObjectOutputStream oos = new ObjectOutputStream(os);
        ObjectInputStream ois = new ObjectInputStream(is);

        //Split user input into parameters

        String[] params = str.split(" +");
        int argc = params.length;



        if(argc != 4){
          System.out.printf("usage: [image_source_path] [filter] [image_destination_path] <technology>\n");
        }else{

          System.out.printf("Sending request...\n");

          source = params[0];       //Relative path to source image
          filter = params[1];       //Filter to apply to image
          destination = params[2];  //Relative path to destination
          technology = params[3];   //Technology to apply the filter

          try{

            //Send object to the Server side of the application

            oos.writeObject(new Request(source, filter, technology));
            oos.flush();

            //Read object from Server side of the application

            Response response = (Response)ois.readObject();

            //Obtain image from Server Response into a byte array

            byte[] byteArray = response.getByteArray();
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(byteArray));

            //Write image to file destination

            ImageIO.write(image,"png", new File(destination + "/" + response.getFileName()));

          }catch(Exception e){
            e.printStackTrace();
          }
        }
      }

      scanner.close();
    }catch(IOException ioe){
      System.out.printf("Could not process request to host: %s\n", host);
    }
  }
}

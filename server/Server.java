/*
*  Server source code: Accepts and handles requests for each client. Includes
*       the Server main thread and a Client Handler
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
import java.awt.*;
import java.util.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Server{

  public Server(){}

  //Main Server thread

  public static void main(String[] args) throws IOException {
    ServerSocket serverSocket = null;
    try{
      serverSocket = new ServerSocket(8991);
      serverSocket.setReuseAddress(true);
      System.out.println("Server started...");

      //Continously accept clients

      while(true){
        Socket socket = serverSocket.accept();
        System.out.println("Client " + socket.getInetAddress().getHostAddress() + " connected");

        //Fork to attend each incoming client request

        ClientHandler clientHandler = new ClientHandler(socket);
        new Thread(clientHandler).start();
      }
    }catch(IOException ioe){
      ioe.printStackTrace();
    }finally{
      if(serverSocket != null){
        try{
          serverSocket.close();
        }catch(IOException ioe){
          ioe.printStackTrace();
        }
      }
    }
  }

  //Handler
  private static class ClientHandler implements Runnable{
    private final Socket socket;

    public ClientHandler(Socket socket){
      this.socket = socket;
    }

    @Override
    public void run(){
      String pathname = "img/";

      String cmd = null;
      String dir = null;
      String fn = null;

      Exec exec = null;

      while(true){
        try{
          //Prepare the IO streams from the Socket's IO stream

          InputStream is = socket.getInputStream();
          OutputStream os = socket.getOutputStream();

          //Prepare the IO streams for Object IO streams

          ObjectInputStream ois = new ObjectInputStream(is);
          ObjectOutputStream oos = new ObjectOutputStream(os);

          //Read Request object obtained from the Client

          Request request = (Request)ois.readObject();

          //Store image byte array from client into local byte array

          byte[] byteArray = request.getByteArray();
          BufferedImage image = ImageIO.read(new ByteArrayInputStream(byteArray));

          //Write image to a directory for further reference to the command executors
          //    (image source)

          ImageIO.write(image,"png", new File(pathname.concat(request.getFileName())));

          //Obtain the technology to use from the Client request

          String techName = request.getTechnology();

          //This section determines which command to execute and to which directory
          //    must it work on

          switch(techName){
            case "cuda":    //Using CUDA

              //New filename to send over Socket
              fn = "cu_" + request.getFilter() + "_" + request.getFileName();

              //Build command: ./Filter [source] [filter] [new_filename]
              cmd = "./Filter ../../img/" + request.getFileName() + " "
                                          + request.getFilter() + " "
                                          + fn;
              //Working directory
              dir = "exec/cu";

              //Build Exec object and execute command
              exec = new Exec(cmd, dir);
              exec.execute();

              break;

            case "java":    //Using Java

              //New filename to send over Socket
              fn = "jv_" + request.getFilter() + "_" + request.getFileName();

              //Build command: java Filter [source] [filter] [new_filename]
              cmd = "java Filter ../../img/" + request.getFileName() + " "
                                             + request.getFilter() + " "
                                             + fn;

              //Working directory
              dir = "exec/jv";

              //Build Exec object and execute command
              exec = new Exec(cmd, dir);
              exec.execute();

              break;

            case "openmp":    //Using OpenMP

              //New filename to send over Socket
              fn = "omp_" + request.getFilter() + "_" + request.getFileName();

              //Build command: java Filter [source] [filter] [new_filename]
              cmd = "./Filter ../../img/" + request.getFileName() + " "
                                          + request.getFilter() + " "
                                          + fn;

              //Working directory
              dir = "exec/omp";

              //Build Exec object and execute command
              exec = new Exec(cmd, dir);
              exec.execute();

              break;

            case "tbb":   //Using Intel Threading Building Blocks

              //New filename to send over Socket
              fn = "tbb_" + request.getFilter() + "_" + request.getFileName();

              //Build command: ./Filter [source] [filter] [new_filename]
              cmd = "./Filter ../../img/" + request.getFileName() + " "
                                         + request.getFilter() + " "
                                         + fn;

              //Working directory
              dir = "exec/tbb";

              //Build Exec object and execute command
              exec = new Exec(cmd, dir);
              exec.execute();

              break;

            default:
              System.out.printf("Option: %s not available\n", techName);
              break;
          }

          //Obtain the image from the previously constructed parameters (source)
          //    and send the Response object through socket back to the client which
          //    contains the image to store on the client side

          oos.writeObject(new Response(dir + "/img/" + fn));
          oos.flush();

        }catch(Exception e){
          //e.printStackTrace();
          System.out.println("Client disconnected...");
          break;
        }
      }
    }
  }
}

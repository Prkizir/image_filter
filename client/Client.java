import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Client{
  public static void main(String[] args){
    String source, destination, filter, technology;

    System.out.println("Welcome:");
    System.out.println("usage: [image_source_path] [filter] [image_destination_path] <technology>");

    String host = "localhost";
    int port = 8991;

    try(Socket socket = new Socket(host,port)){
      OutputStream os = socket.getOutputStream();
      Scanner scanner = new Scanner(System.in);
      String str = null;

      while(true){

        System.out.print("> ");
        str = scanner.nextLine();

        if(str.equals("exit")){
          System.out.println("Closing the connection...");
          socket.close();
          System.out.println("Connection closed");
          break;
        }else{
          ObjectOutputStream oos = new ObjectOutputStream(os);
          String[] params = str.split(" +");
          int argc = params.length;

          if(argc < 3 || argc > 4){
            System.out.println("usage: [image_source_path] [filter] [image_destination_path] <technology>");
          }else{

            System.out.println("Sending request...");
            if(argc == 3){
              source = params[0];
              filter = params[1];
              destination = params[2];

              try{
                oos.flush();
                oos.writeObject(new Request(source, filter));
              }catch(Exception e){
                e.printStackTrace();
              }
            }

            if(argc == 4){
              source = params[0];
              filter = params[1];
              destination = params[2];
              technology = params[3];

              try{
                oos.flush();
                oos.writeObject(new Request(source, filter, technology));
              }catch(Exception e){
                e.printStackTrace();
              }
            }
          }
        }
      }
      scanner.close();
    }catch(IOException ioe){
      System.out.println("Could not process request to host: " + host);
    }
  }
}

import java.net.*;
import java.io.*;
import java.awt.*;
import java.util.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Server{

  public Server(){}

  public static void main(String[] args) throws IOException {
    ServerSocket serverSocket = null;
    try{
      serverSocket = new ServerSocket(8991);
      serverSocket.setReuseAddress(true);

      while(true){
        Socket socket = serverSocket.accept();
        System.out.println("Client " + socket.getInetAddress().getHostAddress() + " connected");

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

      try{
        InputStream is = socket.getInputStream();
        ObjectInputStream ois = new ObjectInputStream(is);

        Request request = (Request)ois.readObject();

        byte[] byteArray = request.getByteArray();
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(byteArray));

        ImageIO.write(image,"png", new File(pathname.concat(request.source)));

      }catch(Exception ioe){
        ioe.printStackTrace();
      }
    }
  }
}

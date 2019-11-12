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
      System.out.println("Server started...");

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

      String cmd = null;
      String dir = null;
      String fn = null;
      Exec exec = null;

      while(true){
        try{
          InputStream is = socket.getInputStream();
          OutputStream os = socket.getOutputStream();

          ObjectInputStream ois = new ObjectInputStream(is);
          ObjectOutputStream oos = new ObjectOutputStream(os);

          Request request = (Request)ois.readObject();

          byte[] byteArray = request.getByteArray();
          BufferedImage image = ImageIO.read(new ByteArrayInputStream(byteArray));

          ImageIO.write(image,"png", new File(pathname.concat(request.getFileName())));

          String techName = request.getTechnology();

          switch(techName){
            case "cuda":
              fn = "cu_" + request.getFilter() + "_" + request.getFileName();
              cmd = "./Filter ../../img/" + request.getFileName() + " "
                                          + request.getFilter() + " "
                                          + fn;
              dir = "exec/cu";

              exec = new Exec(cmd, dir);
              exec.execute();

              break;

            case "java":
              fn = "jv_" + request.getFilter() + "_" + request.getFileName();
              cmd = "java Filter ../../img/" + request.getFileName() + " "
                                             + request.getFilter() + " "
                                             + fn;
              dir = "exec/jv";


              exec = new Exec(cmd, dir);
              exec.execute();

              break;

            case "openmp":
              fn = "omp_" + request.getFilter() + "_" + request.getFileName();
              cmd = "./Filter ../../img/" + request.getFileName() + " "
                                          + request.getFilter() + " "
                                          + fn;
              dir = "exec/omp";

              exec = new Exec(cmd, dir);
              exec.execute();

              break;

            case "tbb":
              fn = "tbb_" + request.getFilter() + "_" + request.getFileName();
              cmd = "./Filter ../../img" + request.getFileName() + " "
                                         + request.getFilter() + " "
                                         + fn;
              dir = "exec/tbb";

              exec = new Exec(cmd, dir);
              exec.execute();

              break;

            default:
              System.out.printf("Option: %s not available\n", techName);
              break;
          }


          oos.writeObject(new Response(dir + "/img/" + fn));
          oos.flush();

        }catch(Exception e){
          e.printStackTrace();
          System.out.println("Client disconnected...");
          break;
        }
      }
    }
  }
}

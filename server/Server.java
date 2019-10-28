import java.net.*;
import java.io.*;

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
      BufferedReader in = null;

      try{
        //Todo

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));



      }catch(IOException ioe){
        ioe.printStackTrace();
      }finally{
        try{
          if(in != null){
            in.close();
          }
        }catch(IOException ioe){
          ioe.printStackTrace();
        }
      }
    }
  }
}

import java.io.IOException;
import java.util.*;

public class Exec{
  String cmd;
  String technology;

  public Exec(String cmd, String technology){
    this.cmd = cmd;
    this.technology = technology;
  }

  public void execute() throws IOException, InterruptedException{
    ProcessBuilder pbuilder = new ProcessBuilder(cmd);

    pbuilder.directory(new File("exec/" + technology));

    
  }
}

import java.io.IOException;
import java.util.*;
import java.io.File;

public class Exec{
  String cmd;
  String dir;

  public Exec(String cmd, String dir){
    this.cmd = cmd;
    this.dir = dir;
  }

  public void execute() throws IOException, InterruptedException{
    List<String> commands = new ArrayList<String>(Arrays.asList(cmd.split(" ")));

    ProcessBuilder pbuilder = new ProcessBuilder(commands);

    pbuilder.directory(new File(dir));

    Process p = pbuilder.start();

    //p.wait();
  }
}

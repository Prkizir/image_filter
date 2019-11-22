/*
*  Exec source code: Process builder. Creates a process that executes a command
      in a given working directory. Runs the filter command in the corresponding
      directory.
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

import java.io.IOException;
import java.util.*;
import java.io.File;

public class Exec{
  String cmd;
  String dir;

  //Constructor

  public Exec(String cmd, String dir){
    this.cmd = cmd;
    this.dir = dir;
  }

  //Executor

  public void execute() throws IOException, InterruptedException{

    //Prepare string into string list ("tokenize")

    List<String> commands = new ArrayList<String>(Arrays.asList(cmd.split(" ")));

    //Create process builder with command list

    ProcessBuilder pbuilder = new ProcessBuilder(commands);

    //Define working directory for process builder

    pbuilder.directory(new File(dir));

    //Start process

    Process p = pbuilder.start();

    //Prevent race conditions

    p.waitFor();
  }
}

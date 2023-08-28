package pluginInterfaces;

import java.math.BigInteger;

interface Execution {

    // Input vars

    // used in build trigger phase
    String exec_token="";

    // used in checking the status of the execution
    String auth_token="";

    boolean start_exec();

    boolean fetch_exec_info();

    boolean kill_exec();
}

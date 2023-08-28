package pluginInterfaces;

import java.math.BigInteger;
import java.net.HttpURLConnection;

interface Connection {

    // Input vars

    // used in build trigger phase
    String exec_token = "";

    // used in checking the status of the execution
    String auth_token = "";

    String read_request_body();

    HttpURLConnection create_connection();
}

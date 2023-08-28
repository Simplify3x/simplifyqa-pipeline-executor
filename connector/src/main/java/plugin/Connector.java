package plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Map;

public class Connector {

    static String ReadRequestbody(String filepath) throws IOException {

        BufferedReader br = null;
        StringBuilder sb = null;
        // System.out.println("\nWorking Directory = " + System.getProperty("user.dir")
        //         + "\n");

        try {
            br = new BufferedReader(new FileReader(new File(filepath)));
            sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null)
                sb.append(output);

            br.close();

            return sb.toString();

        } catch (FileNotFoundException FNFE) {
            if (br != null)
                br.close();
            System.out.println(
                    "\nThe expected template file for the request body at path: " + filepath + " was not found.");
            FNFE.printStackTrace();

            return null;
        }
    }

    public static HttpURLConnection create_connection(
            String api,
            Map<String, String> request_headers,
            String request_body,
            String conn_type) throws IOException {

        URL url_obj = new URL(api);
        HttpURLConnection post_connection = (HttpURLConnection) url_obj.openConnection();
        System.setProperty("http.keepAlive", "true");

        // SETTING THE REQUEST PROPERTIES

        // request type
        post_connection.setRequestMethod(conn_type);

        // SETTING REQUEST HEADERS
        post_connection.setDoOutput(true);
        if (request_headers != null)
            for (Map.Entry<String, String> entry : request_headers.entrySet())
                post_connection.setRequestProperty(
                        entry.getKey(),
                        entry.getValue());

        // SETTING REQUEST BODY
        // String post_params = new Gson()
        // .toJson(
        // request_body,
        // (new TypeToken<Map<String, Object>>() {
        // }.getType()));

        OutputStream output_stream = post_connection.getOutputStream();
        output_stream.write(request_body.getBytes(Charset.defaultCharset()));
        output_stream.flush();
        output_stream.close();

        try {
            if (post_connection != null && (post_connection.getResponseCode() >= 199
                    || post_connection.getResponseCode() <= 399))
                return post_connection;
            else
                return null;
        } catch (UnknownHostException UHE) {
            post_connection.disconnect();
            UHE.printStackTrace();
            return null;
        }

    }
}

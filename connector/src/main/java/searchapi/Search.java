package searchapi;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

import org.json.JSONObject;
import plugin.Connector;
import plugin.Executor;

public class Search {
    static String search(String api, Map<String, String> req_headers, String req_body) {
        return "";
    }

    public static int getTotalTcSuite(String api, Map<String, String> req_headers, String req_body) {

        int total_count = -1;
        String resp = null;

        try {
            HttpURLConnection conn = Connector.create_connection(api, req_headers, req_body, "POST");
            resp = Executor.get_response(conn);
            total_count = new JSONObject(
                    new JSONObject(new JSONObject(resp).get("data").toString()).getJSONArray("data").get(0).toString())
                    .getJSONArray("testCases").length();

            return total_count;
        } catch (IOException e) {

            return -1;
        }

    }
}

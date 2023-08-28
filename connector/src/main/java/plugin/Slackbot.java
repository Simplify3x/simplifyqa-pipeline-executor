package plugin;

import java.io.IOException;
import java.net.HttpURLConnection;

public class Slackbot {
    static String webhook_url = "";
    String slack_req_body_path = "";

    static String success_accent_color = "#03fc30";
    static String failure_accent_color = "#fc0339";

    String exec_id, suite_id;
    double fail_percent;

    static boolean sendlogs(boolean status, String exec_id, String suite_id, Double fail_percent, String report_url,
            String logs) {

        String payload, resp, message;
        HttpURLConnection conn = null;
        logs = logs.replace("\n", "\\n");
        logs = logs.replace("\"", "\\\"");
        try {
            payload = Connector.ReadRequestbody(Executor.slack_req_body_path);
            // payload = payload.replace("#logs", logs);

            if (!Slackbot.webhook_url.equals("")) {

                message = "```Execution ID: " + exec_id + "\\nSuite ID: " + suite_id + "\\nFail Percentage: "
                        + String.format("%2.2f", fail_percent) + " %```";// + "\\n" + logs;
                payload = payload.replace("#message", message);
                payload = payload.replace("#reportURL", report_url);

                if (status) {
                    payload = payload.replace("#header", "Execution Passed!");
                    payload = payload.replace("#accent", Slackbot.success_accent_color);
                } else {
                    payload = payload.replace("#header", "Execution Failed!");
                    payload = payload.replace("#accent", Slackbot.failure_accent_color);
                }

                conn = Connector.create_connection(Slackbot.webhook_url, null, payload, "POST");
                resp = Executor.get_response(conn);

                if (resp.equals("ok"))
                    return true;
                else
                    return false;

            } else
                return false;

        } catch (IOException IOE) {
            IOE.printStackTrace();
            System.out.println(
                    "\nWARNING: Failed to export logs to Slack Channel.");

            return false;
        }
    }
}

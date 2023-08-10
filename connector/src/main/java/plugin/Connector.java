package plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Connector {

	// Vulnerabilities identified so far:-
	// 1. Cannot find an alternative to hudson.util.Secret which is used to conceal
	// the token and the auth key
	// 2. Cannot find an alternatice to hudson.model.TaskListener which is used to
	// log the process

	// API LINKS
	private static String build_task_api, check_status_api;
	private String temp;

	StringBuilder logs = new StringBuilder();
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

	private Map<String, String> request_headers = new HashMap<String, String>();
	private Map<String, Object> build_task_request_body = new HashMap<String, Object>();
	private Map<String, Object> check_status_request_body = new HashMap<String, Object>();

	JSONObject exec_info = new JSONObject();
	private static int WAIT_TIME = 3000;

	// connector constructor called by the ci/cd
	Connector(String token, String build_task_api, String check_status_api) throws IOException, ParseException {

		Connector.build_task_api = build_task_api;
		Connector.check_status_api = check_status_api;

		request_headers.put("Content-Type", "application/json");
		build_task_request_body.put("token", token);

		HttpURLConnection post_connection = this.create_connection(Connector.build_task_api, request_headers,
				build_task_request_body, "POST");

		if (post_connection != null) {
			temp = "[" + dtf.format(LocalDateTime.now()) + "] "
					+ "EXECUTION STATUS: CONNECTION ESTABLISHED SUCCESSFULLY!" + "\n\n";
			System.out.print(temp);
			logs.append(temp);
			String response = this.get_response(post_connection);
			Map<String, Object> map = new ObjectMapper().readValue(response.toString(),
					new TypeReference<Map<String, Object>>() {
					});

			// REQUEST BODY FOR CHECK TESTCASE STATUS API CALL
			// check_status_request_body.put("token", token);
			check_status_request_body.put("success", Boolean.parseBoolean(map.get("success").toString()));
			check_status_request_body.put("executionId", Long.parseLong(map.get("executionId").toString()));
			request_headers.put("Authorization", map.get("authKey").toString());
			check_status_request_body.put("projectId", Integer.parseInt(map.get("projectId").toString()));
			check_status_request_body.put("customerId", Integer.parseInt(map.get("customerId").toString()));
		} else {
			temp = "[" + dtf.format(LocalDateTime.now()) + "] "
					+ "EXECUTION STATUS: CONNECTION ERROR!" + "\n\n";
			System.out.print(temp);
			logs.append(temp);
		}

		post_connection.disconnect();
	}

	private String get_response(HttpURLConnection post_connection) throws IOException {

		StringBuilder sb = new StringBuilder();
		String output = "";
		BufferedReader br;

		if (post_connection != null) {
			if (199 >= post_connection.getResponseCode() || post_connection.getResponseCode() <= 399) {
				br = new BufferedReader(
						new InputStreamReader(post_connection.getInputStream(), Charset.forName("UTF-8")));
			} else {
				br = new BufferedReader(
						new InputStreamReader(post_connection.getErrorStream(), Charset.forName("UTF-8")));
			}

			while ((output = br.readLine()) != null) {
				// Uncomment to check the responses
				// System.out.println(output);
				sb.append(output);
			}
			br.close();
		}

		return sb.toString();
	}

	private HttpURLConnection create_connection(
			String api,
			Map<String, String> request_headers,
			Map<String, Object> request_body,
			String conn_type) throws IOException {

		URL url_obj = new URL(api);
		HttpURLConnection post_connection = (HttpURLConnection) url_obj.openConnection();
		System.setProperty("http.keepAlive", "true");

		// SETTING THE REQUEST PROPERTIES

		// request type
		post_connection.setRequestMethod(conn_type);

		// SETTING REQUEST HEADERS
		post_connection.setDoOutput(true);
		for (Map.Entry<String, String> entry : request_headers.entrySet())
			post_connection.setRequestProperty(
					entry.getKey(),
					entry.getValue());

		// SETTING REQUEST BODY
		String post_params = new Gson()
				.toJson(
						request_body,
						(new TypeToken<Map<String, Object>>() {
						}.getType()));

		OutputStream output_stream = post_connection.getOutputStream();
		output_stream.write(post_params.getBytes(Charset.defaultCharset()));
		output_stream.flush();
		output_stream.close();

		if (post_connection != null && (post_connection.getResponseCode() >= 199
				|| post_connection.getResponseCode() <= 399))
			return post_connection;
		else
			return null;
	}

	boolean getExecInfo() throws IOException, InterruptedException, JSONException {

		HttpURLConnection conn = null;

		while (conn == null)
			conn = this.create_connection(Connector.check_status_api, request_headers, check_status_request_body,
					"POST");

		if (conn.getResponseCode() >= 199 || conn.getResponseCode() <= 399) {
			this.exec_info = new JSONObject(this.get_response(conn));
			temp = "[" + dtf.format(LocalDateTime.now()) + "] "
					+ "EXECUTION STATUS: test suite execution triggered successfully" + "\n\n";
			System.out.print(temp);
			logs.append(temp);

			// To check whether execution data is available or not
			while (!exec_info.keySet().contains("data")) {
				Thread.sleep(Connector.WAIT_TIME);
				conn = this.create_connection(Connector.check_status_api, request_headers, check_status_request_body,
						"POST");
				this.exec_info = new JSONObject(this.get_response(conn));
				temp = "[" + dtf.format(LocalDateTime.now()) + "] "
						+ "EXECUTION STATUS: initializing testcases in the triggered suite" + "\n\n";

				// Also including the response body in the logs
				temp = temp + "[" + dtf.format(LocalDateTime.now()) + "] "
						+ "RESPONSE BODY: " + this.exec_info.toString() + "\n\n";

				System.out.print(temp);
				logs.append(temp);
			}

			// To check whether execution is in-progress or completed
			while (!exec_info.getJSONObject("data").getJSONObject("data").get("execution").equals("COMPLETED")) {
				Thread.sleep(Connector.WAIT_TIME);
				conn = this.create_connection(Connector.check_status_api, request_headers, check_status_request_body,
						"POST");
				this.exec_info = new JSONObject(this.get_response(conn));
				temp = "[" + dtf.format(LocalDateTime.now()) + "] "
						+ "EXECUTION STATUS: execution of testcase id: "
						+ ((JSONObject) new JSONArray(
								exec_info.getJSONObject("data").getJSONObject("data").get("result").toString()).get(0))
								.get("tcCode")
						+ " in-progress" + "\n\n";

				// Also including the response body in the logs
				temp = temp + "[" + dtf.format(LocalDateTime.now()) + "] "
						+ "RESPONSE BODY: " + this.exec_info.toString() + "\n\n";

				System.out.print(temp);
				logs.append(temp);
			}

			conn = this.create_connection(Connector.check_status_api, request_headers, check_status_request_body,
					"POST");
			this.exec_info = new JSONObject(this.get_response(conn));
			temp = "[" + dtf.format(LocalDateTime.now()) + "] "
					+ "EXECUTION STATUS: suite execution completed" + "\n\n";
			System.out.print(temp);
			logs.append(temp);

			temp = "[" + dtf.format(LocalDateTime.now()) + "] "
					+ "EXECUTION STATUS: Execution report can be retrieved from the following url: "
					+ exec_info.getJSONObject("data").getJSONObject("data").get("reporturl").toString()
					+ "\n\n";
			System.out.print(temp);
			logs.append(temp);

			return true;
		} else
			return false;
	}
}

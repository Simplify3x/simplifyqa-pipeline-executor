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
	private String temp, info_str = "";

	StringBuilder logs = new StringBuilder();
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

	private Map<String, String> request_headers = new HashMap<String, String>();
	private Map<String, Object> build_task_request_body = new HashMap<String, Object>();
	private Map<String, Object> check_status_request_body = new HashMap<String, Object>();
	private Map<Integer, Integer> steps_per_tc = new HashMap<Integer, Integer>();

	JSONObject exec_info = new JSONObject();
	private int WAIT_TIME = 3000;
	private double execution_percent;

	// connector constructor called by the ci/cd
	Connector(String token, String build_task_api, String check_status_api, int WAIT_TIME)
			throws IOException, ParseException {

		// poll rate for logs
		this.WAIT_TIME = WAIT_TIME;

		Connector.build_task_api = build_task_api;
		Connector.check_status_api = check_status_api;

		request_headers.put("Content-Type", "application/json");
		request_headers.put("Connection", "keep-alive");
		build_task_request_body.put("token", token);

		HttpURLConnection post_connection = this.create_connection(Connector.build_task_api, request_headers,
				build_task_request_body, "POST");

		if (post_connection != null) {
			this.temp = "[" + dtf.format(LocalDateTime.now()) + "] "
					+ "EXECUTION STATUS: CONNECTION ESTABLISHED SUCCESSFULLY!" + "\n\n";
			System.out.print(this.temp);
			logs.append(this.temp);
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
			this.temp = "[" + dtf.format(LocalDateTime.now()) + "] "
					+ "EXECUTION STATUS: CONNECTION ERROR!" + "\n\n";
			System.out.print(this.temp);
			logs.append(this.temp);
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

	boolean getExecInfo(boolean include_response_body) throws IOException, InterruptedException, JSONException {

		int total_tc_count = 0, executed_tc_count = 0, totalSteps, executed_steps, prev_executed_tc_count = -1;
		int tc_total_steps, current_step;
		String tc_name, tc_id;
		HttpURLConnection conn = null;
		Map<Integer, Boolean> progress = new HashMap<Integer, Boolean>();

		boolean exec_flag;

		while (conn == null)
			conn = this.create_connection(Connector.check_status_api, request_headers, check_status_request_body,
					"POST");

		if (conn.getResponseCode() >= 199 || conn.getResponseCode() <= 399) {
			this.exec_info = new JSONObject(this.get_response(conn));
			this.temp = "[" + dtf.format(LocalDateTime.now()) + "] "
					+ "EXECUTION STATUS: test suite EXECUTION TRIGGERED successfully" + "\n\n";
			System.out.print(this.temp);
			logs.append(this.temp);

			// To check whether execution data is available or not
			while (!exec_info.keySet().contains("data")) {
				Thread.sleep(this.WAIT_TIME);
				conn = this.create_connection(Connector.check_status_api, request_headers, check_status_request_body,
						"POST");
				this.exec_info = new JSONObject(this.get_response(conn));

				this.temp = "[" + dtf.format(LocalDateTime.now()) + "] "
						+ "EXECUTION STATUS: INITIALIZING TESTCASES in the triggered suite" + "\n\n";

				// Also including the response body in the logs
				if (include_response_body)
					this.temp = this.temp + "[" + dtf.format(LocalDateTime.now()) + "] "
							+ "RESPONSE BODY: " + this.exec_info.toString() + "\n\n";

				System.out.print(this.temp);
				logs.append(this.temp);

			}

			// Before the starting of all testcases
			this.execution_percent = 0.00;

			// To check whether execution is in-progress or completed
			while (!(exec_info.getJSONObject("data").getJSONObject("data").get("execution").toString().toUpperCase())
					.equals("COMPLETED")) {

				executed_steps = 0;
				Thread.sleep(this.WAIT_TIME);

				conn = this.create_connection(Connector.check_status_api, request_headers, check_status_request_body,
						"POST");

				this.exec_info = new JSONObject(this.get_response(conn));

				total_tc_count = new JSONArray(
						this.exec_info.getJSONObject("data").getJSONObject("data").get("result").toString()).length();

				// this.temp = "[" + dtf.format(LocalDateTime.now()) + "] "
				// + "EXECUTION STATUS: suite EXECUTION IN-PROGRESS with completion rate of "
				// + String.format("%.2f", this.execution_percent) + " % (executed " +
				// executed_tc_count + " of "
				// + total_tc_count + " testcase(s))\n";

				totalSteps = 0;
				executed_tc_count = 0;
				info_str = "";
				// Iterating through all the response of all the testcases in the suite
				for (int i = 0; i < total_tc_count; i++) {

					tc_total_steps = Integer.parseInt(new JSONObject(new JSONArray(
							this.exec_info.getJSONObject("data").getJSONObject("data").get("result").toString()).get(i)
							.toString())
							.get("totalSteps").toString());

					current_step = Integer.parseInt(new JSONObject(new JSONArray(
							this.exec_info.getJSONObject("data").getJSONObject("data").get("result").toString()).get(i)
							.toString())
							.get("totalSteps").toString());

					executed_steps = executed_steps + current_step;

					tc_name = new JSONObject(new JSONArray(
							this.exec_info.getJSONObject("data").getJSONObject("data").get("result").toString()).get(i)
							.toString())
							.get("tcName").toString();

					tc_id = new JSONObject(new JSONArray(
							this.exec_info.getJSONObject("data").getJSONObject("data").get("result").toString()).get(i)
							.toString())
							.get("tcCode").toString();

					this.steps_per_tc.put(i, tc_total_steps);
					totalSteps = totalSteps + tc_total_steps;

					this.info_str = this.info_str + "\n                      ";
					if (new JSONObject(new JSONArray(
							this.exec_info.getJSONObject("data").getJSONObject("data").get("result").toString()).get(i)
							.toString()).get("result").toString().equals("Inprogress")) {

						this.info_str = this.info_str + tc_id + ": " + tc_name
								+ " | TESTCASE IN-PROGRESS (total steps: "
								+ tc_total_steps + ")";

						progress.put(i, false);

						// this.temp = this.temp + tc_id + ": " + tc_name + " | TESTCASE IN-PROGRESS
						// with
						// completion rate of "
						// + String.format("%.2f", ((current_step / tc_total_steps) * 100.00)) + " %
						// (executed "
						// + current_step + " of " + tc_total_steps + " steps)";

					} else if (new JSONObject(new JSONArray(
							this.exec_info.getJSONObject("data").getJSONObject("data").get("result").toString()).get(i)
							.toString()).get("result").toString().equals("Passed")) {

						this.info_str = this.info_str + tc_id + ": " + tc_name + " | TESTCASE PASSED (total steps: "
								+ tc_total_steps + ")";
						executed_tc_count++;
						progress.put(i, true);

						// this.temp = this.temp + tc_id + ": " + tc_name + " | TESTCASE PASSED with
						// completion
						// rate of "
						// + String.format("%.2f", ((current_step / tc_total_steps) * 100.00)) + " %
						// (executed "
						// + current_step + " of " + tc_total_steps + " steps)";
					} else {

						this.info_str = this.info_str + tc_id + ": " + tc_name + " | TESTCASE FAILED (total steps: "
								+ tc_total_steps + ")";

						progress.put(i, false);

						// this.temp = this.temp + tc_id + ": " + tc_name + " | TESTCASE FAILED with
						// completion
						// rate of "
						// + String.format("%.2f", ((current_step / tc_total_steps) * 100.00)) + " %
						// (executed "
						// + current_step + " of " + tc_total_steps + " steps)";
					}
				}

				this.execution_percent = (executed_steps / totalSteps) * 100.00;
				this.info_str = this.info_str + "\n\n";

				// Also including the response body in the logs
				if (include_response_body)
					this.info_str = this.info_str + "[" + dtf.format(LocalDateTime.now()) + "] "
							+ "RESPONSE BODY: " + this.exec_info.toString() + "\n\n";

				this.temp = "[" + dtf.format(LocalDateTime.now()) + "] "
						+ "EXECUTION STATUS: suite EXECUTION IN-PROGRESS (executed " + executed_tc_count + " of "
						+ total_tc_count + " testcase(s))\n" + this.info_str;

				if (prev_executed_tc_count < executed_tc_count) {
					System.out.print(this.temp);
					logs.append(this.temp);
					prev_executed_tc_count = executed_tc_count;
				}
			}

			System.out.print(this.temp);
			logs.append(this.temp);
			prev_executed_tc_count = executed_tc_count;

			conn = this.create_connection(Connector.check_status_api, request_headers, check_status_request_body,
					"POST");
			this.exec_info = new JSONObject(this.get_response(conn));

			if (executed_tc_count == total_tc_count) {

				this.temp = "[" + dtf.format(LocalDateTime.now()) + "] "
						+ "EXECUTION STATUS: suite EXECUTION COMPLETED (fail percentage: 0.00 % )\n\n";
				exec_flag = true;

			} else {

				this.temp = "[" + dtf.format(LocalDateTime.now()) + "] "
						+ "EXECUTION STATUS: suite EXECUTION FAILED (fail percentage: "
						+ String.format("%.2f",
								((Double.valueOf(executed_tc_count) / Double.valueOf(total_tc_count)) * 100.00))
						+ " % )\n\n";
				exec_flag = false;

			}

			this.temp = this.temp + "[" + dtf.format(LocalDateTime.now()) + "] "
					+ "EXECUTION STATUS: Execution report can be retrieved from the following url: "
					+ exec_info.getJSONObject("data").getJSONObject("data").get("reporturl").toString()
					+ "\n\n";

			System.out.print(this.temp);
			logs.append(this.temp);

			return exec_flag;
		} else {
			this.temp = "[" + dtf.format(LocalDateTime.now()) + "] "
					+ "EXECUTION STATUS: test suite EXECUTION TRIGGER FAILED (due to bad response code)" + "\n\n";
			System.out.print(this.temp);
			logs.append(this.temp);

			return false;
		}
	}
}

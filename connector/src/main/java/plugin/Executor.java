package plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;
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

import searchapi.Search;

public class Executor {

	// Vulnerabilities identified so far:-

	// 1. Cannot find an alternative to hudson.util.Secret which is used to conceal
	// the token and the auth key

	// 2. Cannot find an alternatice to hudson.model.TaskListener which is used to
	// log the process

	// Request body resources paths
	private static String req_temp_path = "src/main/resources/templates/requests/";
	private static String build_task_req_body_path = req_temp_path + "build_task_request_body.json.txt";
	private static String check_status_req_body_path = req_temp_path + "check_status_request_body.json.txt";
	private static String search_api_req_body_path = req_temp_path + "search_api.json.txt";
	private static String kill_exec_api_req_body_path = req_temp_path + "kill_exec_api.json.txt";
	static String slack_req_body_path = req_temp_path + "slack_webhook_api.json.txt";

	private String build_task_request_body, check_status_request_body, search_api_request_body;

	// API LINKS
	private static String build_task_api, check_status_api, search_api, kill_exec_api;
	private String temp = "", info_str = "", report_url = "";
	private String exec_id, suite_id, customer_id;
	private double fail_percent;

	StringBuilder logs = new StringBuilder();
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

	private Map<String, String> request_headers = new HashMap<String, String>();
	private Map<Integer, Integer> steps_per_tc = new HashMap<Integer, Integer>();
	private Map<String, Object> map;

	JSONObject exec_info = new JSONObject();
	private int POLL_RATE = 3000;
	private double threshold, current_threshold = 0.00;
	private int total_tc_count;

	// connector constructor called by the ci/cd
	Executor(String token, String build_task_api, String check_status_api, String kill_exec_api,
			int POLL_RATE,
			int threshold)
			throws IOException, ParseException {

		// poll rate for logs
		this.POLL_RATE = POLL_RATE;

		Executor.build_task_api = build_task_api;
		Executor.check_status_api = check_status_api;
		// Executor.search_api = search_api;
		Executor.kill_exec_api = kill_exec_api;

		this.request_headers.put("Content-Type", "application/json");
		this.request_headers.put("Connection", "keep-alive");
		this.build_task_request_body = Connector.ReadRequestbody(Executor.build_task_req_body_path).replace("#token",
				token);

		HttpURLConnection post_connection = Connector.create_connection(Executor.build_task_api, this.request_headers,
				this.build_task_request_body, "POST");

		if (post_connection != null) {
			this.temp = "[" + dtf.format(LocalDateTime.now()) + "] "
					+ "EXECUTION STATUS: CONNECTION ESTABLISHED SUCCESSFULLY!" + "\n\n";
			System.out.print(this.temp);
			this.logs.append(this.temp);
			String response = Executor.get_response(post_connection);
			this.map = new ObjectMapper().readValue(response.toString(),
					new TypeReference<Map<String, Object>>() {
					});

			this.exec_id = this.map.get("executionId").toString();
			this.customer_id = this.map.get("customerId").toString();

			if (!Boolean.parseBoolean(this.map.get("success").toString())) {
				this.temp = "[" + dtf.format(LocalDateTime.now()) + "] "
						+ "EXECUTION STATUS: failed due to the following reason: " + this.map.get("error").toString()
						+ "\n\n";

				if (Slackbot.sendlogs(false, "EX-" + this.customer_id + this.exec_id, this.suite_id,
						this.current_threshold,
						this.report_url,
						this.logs.toString()))
					this.temp = "[" + dtf.format(LocalDateTime.now()) + "] "
							+ "EXECUTION STATUS: Slack notification sent SUCCESSFULLY!" + "\n\n"
							+ this.temp;
				else
					this.temp = "[" + dtf.format(LocalDateTime.now()) + "] "
							+ "EXECUTION STATUS: Slack notification send FAILED!" + "\n\n"
							+ this.temp;

				System.out.print(this.temp);
				this.logs.append(this.temp);
				System.out.println("EXECUTION FAILED: Exiting with a non-zero exit code.");
				System.exit(1);
			}

			// REQUEST BODY FOR CHECK TESTCASE STATUS API CALL
			// check_status_request_body.put("token", token);
			this.request_headers.put("Authorization", this.map.get("authKey").toString());

			this.check_status_request_body = Connector.ReadRequestbody(Executor.check_status_req_body_path);
			this.check_status_request_body = this.check_status_request_body.replace("#success",
					this.map.get("success").toString());
			this.check_status_request_body = this.check_status_request_body.replace("#executionId",
					this.map.get("executionId").toString());
			this.check_status_request_body = this.check_status_request_body.replace("#projectId",
					this.map.get("projectId").toString());
			this.check_status_request_body = this.check_status_request_body.replace("#customerId",
					this.map.get("customerId").toString());

			// this.search_api_request_body =
			// Connector.ReadRequestbody(Executor.search_api_req_body_path);

			// this.search_api_request_body =
			// this.search_api_request_body.replace("#customerId",
			// this.map.get("customerId").toString());
			// this.search_api_request_body =
			// this.search_api_request_body.replace("#projectId",
			// this.map.get("projectId").toString());
			// this.search_api_request_body =
			// this.search_api_request_body.replace("#suiteId", suite_id);

			// this.total_tc_count = Search.getTotalTcSuite(search_api,
			// this.request_headers,
			// this.search_api_request_body);

		} else {
			this.temp = "[" + dtf.format(LocalDateTime.now()) + "] "
					+ "EXECUTION STATUS: CONNECTION ERROR!" + "\n\n";

			if (Slackbot.sendlogs(false, "EX-" + this.customer_id + this.exec_id, this.suite_id, this.current_threshold,
					this.report_url,
					this.logs.toString()))
				this.temp = "[" + dtf.format(LocalDateTime.now()) + "] "
						+ "EXECUTION STATUS: Slack notification sent SUCCESSFULLY!" + "\n\n"
						+ this.temp;
			else
				this.temp = "[" + dtf.format(LocalDateTime.now()) + "] "
						+ "EXECUTION STATUS: Slack notification send FAILED!" + "\n\n"
						+ this.temp;

			System.out.print(this.temp);
			this.logs.append(this.temp);

			System.out.println("EXECUTION FAILED: Exiting with a non-zero exit code.");
			System.exit(1);
		}

		post_connection.disconnect();
	}

	public static String get_response(HttpURLConnection post_connection) throws IOException {

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

	boolean killExec() {

		HttpURLConnection conn = null;
		String payload = null, resp = null;

		try {
			payload = Connector.ReadRequestbody(Executor.kill_exec_api_req_body_path);
			payload = payload.replace("#executionId", this.map.get("executionId").toString());
			payload = payload.replace("#customerId", this.map.get("customerId").toString());
			// payload = payload.replace("#userId", map.get("userId").toString());
			// payload = payload.replace("#userName", "");

			if (payload != null) {

				while (conn == null)
					conn = Connector.create_connection(Executor.kill_exec_api, this.request_headers, payload, "POST");

				resp = Executor.get_response(conn);

				if ((new JSONObject(resp).keySet().contains("sucess")))
					if (Boolean.parseBoolean(new JSONObject(resp).get("sucess").toString()))
						return true;

				return false;

			} else {
				return false;
			}

		} catch (IOException IOE) {
			// IOE.printStackTrace();
			return false;
		} catch (JSONException JOE) {
			// JOE.printStackTrace();
			return false;
		}
	}

	boolean getExecInfo(boolean include_request_body, boolean include_response_body)
			throws IOException, InterruptedException, JSONException {

		int total_tc_count = this.total_tc_count, limit = 0, totalSteps, executed_steps, executed_tc_count = 0,
				prev_executed_tc_count = -1;
		int tc_total_steps, current_step;
		String tc_name, tc_id;
		HttpURLConnection conn = null;
		Map<Integer, Boolean> progress = new HashMap<Integer, Boolean>();

		boolean exec_flag;

		while (conn == null) {
			try {
				conn = Connector.create_connection(Executor.check_status_api, request_headers,
						this.check_status_request_body,
						"POST");

				this.exec_info = new JSONObject(Executor.get_response(conn));
			} catch (UnknownHostException UHE) {
				conn.disconnect();
				UHE.printStackTrace();
			}

		}

		if (conn.getResponseCode() >= 199 || conn.getResponseCode() <= 399) {
			// this.exec_info = new JSONObject(Executor.get_response(conn));
			this.temp = "[" + dtf.format(LocalDateTime.now()) + "] "
					+ "EXECUTION STATUS: test suite EXECUTION TRIGGERED successfully" + "\n\n";
			System.out.print(this.temp);
			this.logs.append(this.temp);

			// To check whether execution data is available or not
			while (!exec_info.keySet().contains("data")) {
				Thread.sleep(this.POLL_RATE);
				conn = Connector.create_connection(Executor.check_status_api, request_headers,
						this.check_status_request_body,
						"POST");
				this.exec_info = new JSONObject(Executor.get_response(conn));

				this.temp = "[" + dtf.format(LocalDateTime.now()) + "] "
						+ "EXECUTION STATUS: INITIALIZING TESTCASES in the triggered suite" + "\n\n";

				if (include_request_body)
					this.temp = this.temp + "[" + dtf.format(LocalDateTime.now()) + "] "
							+ "REQUEST BODY: " + this.check_status_request_body.toString() + "\n\n";

				if (include_response_body)
					this.temp = this.temp + "[" + dtf.format(LocalDateTime.now()) + "] "
							+ "RESPONSE BODY: " + this.exec_info.toString() + "\n\n";

				System.out.print(this.temp);
				this.logs.append(this.temp);

			}

			// Before the starting of all testcases
			// this.execution_percent = 0.00;
			this.suite_id = exec_info.getJSONObject("data").getJSONObject("data").get("suiteId").toString();
			this.total_tc_count = Integer
					.parseInt(exec_info.getJSONObject("data").getJSONObject("data").get("totalTestcases").toString());

			if (threshold < 0)
				this.threshold = Double.valueOf((1.00 / total_tc_count) * 100.00);

			// To check whether execution is in-progress or completed
			while (!(exec_info.getJSONObject("data").getJSONObject("data").get("execution").toString().toUpperCase())
					.equals("COMPLETED")
					&& this.threshold >= this.current_threshold) {

				this.current_threshold = ((Double.valueOf(limit - executed_tc_count)
						/ Double.valueOf(total_tc_count)) * 100.00);
				executed_steps = 0;
				Thread.sleep(this.POLL_RATE);

				try {
					conn = Connector.create_connection(Executor.check_status_api, request_headers,
							this.check_status_request_body,
							"POST");

					this.exec_info = new JSONObject(Executor.get_response(conn));
				} catch (UnknownHostException UHE) {
					conn.disconnect();
					UHE.printStackTrace();
				}

				limit = new JSONArray(
						this.exec_info.getJSONObject("data").getJSONObject("data").get("result").toString()).length();

				// this.temp = "[" + dtf.format(LocalDateTime.now()) + "] "
				// + "EXECUTION STATUS: suite EXECUTION IN-PROGRESS with completion rate of "
				// + String.format("%2.2f", this.execution_percent) + " % (executed " +
				// executed_tc_count + " of "
				// + total_tc_count + " testcase(s))\n";

				totalSteps = 0;
				executed_tc_count = 0;
				info_str = "";
				// Iterating through all the response of all the testcases in the suite
				for (int i = 0; i < limit; i++) {

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
						// + String.format("%2.2f", ((current_step / tc_total_steps) * 100.00)) + " %
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
						// + String.format("%2.2f", ((current_step / tc_total_steps) * 100.00)) + " %
						// (executed "
						// + current_step + " of " + tc_total_steps + " steps)";
					} else {

						this.info_str = this.info_str + tc_id + ": " + tc_name + " | TESTCASE FAILED (total steps: "
								+ tc_total_steps + ")";

						progress.put(i, false);

						// this.temp = this.temp + tc_id + ": " + tc_name + " | TESTCASE FAILED with
						// completion
						// rate of "
						// + String.format("%2.2f", ((current_step / tc_total_steps) * 100.00)) + " %
						// (executed "
						// + current_step + " of " + tc_total_steps + " steps)";
					}
				}

				// this.execution_percent = (executed_steps / totalSteps) * 100.00;
				this.info_str = this.info_str + "\n\n";

				if (include_request_body)
					this.info_str = this.info_str + "[" + dtf.format(LocalDateTime.now()) + "] "
							+ "REQUEST BODY: " + this.check_status_request_body.toString() + "\n\n";

				if (include_response_body)
					this.info_str = this.info_str + "[" + dtf.format(LocalDateTime.now()) + "] "
							+ "RESPONSE BODY: " + this.exec_info.toString() + "\n\n";

				this.temp = "[" + dtf.format(LocalDateTime.now()) + "] "
						+ "EXECUTION STATUS: suite EXECUTION IN-PROGRESS (executed " + executed_tc_count + " of "
						+ total_tc_count + " testcase(s))\n" + this.info_str;

				if (prev_executed_tc_count < executed_tc_count) {
					System.out.print(this.temp);
					this.logs.append(this.temp);
					prev_executed_tc_count = executed_tc_count;
				}

				info_str = "";
			}

			System.out.print(this.temp);
			this.logs.append(this.temp);
			prev_executed_tc_count = executed_tc_count;

			this.temp = "";
			if (this.threshold <= this.current_threshold) {
				if (!this.killExec()) {
					this.temp = "[" + dtf.format(LocalDateTime.now()) + "] "
							+ "EXECUTION STATUS: Failed to explicitly kill the execution\n\n";
				}
			}

			try {
				conn = Connector.create_connection(Executor.check_status_api, request_headers,
						this.check_status_request_body,
						"POST");
				this.exec_info = new JSONObject(Executor.get_response(conn));
			} catch (UnknownHostException UHE) {
				conn.disconnect();
				UHE.printStackTrace();
			}

			if (this.threshold < 0.00) {
				this.threshold = Double.valueOf(1.00 / Double.valueOf(total_tc_count))
						* 100.00;
			}

			if (this.threshold >= this.current_threshold) {

				this.temp = this.temp + "[" + dtf.format(LocalDateTime.now()) + "] "
						+ "EXECUTION STATUS: suite EXECUTION PASSED (fail percentage: "
						+ String.format("%2.2f", this.current_threshold) + " % )\n\n";

				exec_flag = true;

			} else {

				this.temp = this.temp + "[" + dtf.format(LocalDateTime.now()) + "] "
						+ "EXECUTION STATUS: suite EXECUTION FAILED (fail percentage: "
						+ String.format("%2.2f", this.current_threshold) + " % )\n\n";

				exec_flag = false;

			}
			this.report_url = exec_info.getJSONObject("data").getJSONObject("data").get("reporturl").toString();
			this.temp = this.temp + "[" + dtf.format(LocalDateTime.now()) + "] "
					+ "EXECUTION STATUS: Execution report can be retrieved from the following url: "
					+ this.report_url
					+ "\n\n";

			if (Slackbot.sendlogs(exec_flag, "EX-" + this.customer_id + this.exec_id, this.suite_id,
					this.current_threshold,
					this.report_url,
					this.logs.toString()))
				this.temp = "[" + dtf.format(LocalDateTime.now()) + "] "
						+ "EXECUTION STATUS: Slack notification sent SUCCESSFULLY!" + "\n\n"
						+ this.temp;
			else
				this.temp = "[" + dtf.format(LocalDateTime.now()) + "] "
						+ "EXECUTION STATUS: Slack notification send FAILED!" + "\n\n"
						+ this.temp;

			System.out.print(this.temp);
			this.logs.append(this.temp);

			return exec_flag;
		} else {
			this.exec_info = new JSONObject(Executor.get_response(conn));

			if (include_request_body)
				this.temp = this.temp + "[" + dtf.format(LocalDateTime.now()) + "] "
						+ "REQUEST BODY: " + this.check_status_request_body.toString() + "\n\n";

			if (include_response_body)
				this.temp = "[" + dtf.format(LocalDateTime.now()) + "] "
						+ "RESPONSE BODY: " + this.exec_info.toString() + "\n\n";

			this.temp = "[" + dtf.format(LocalDateTime.now()) + "] "
					+ "EXECUTION STATUS: test suite EXECUTION TRIGGER FAILED (due to bad response code)" + "\n\n"
					+ this.temp;

			if (Slackbot.sendlogs(false, "EX-" + this.customer_id + this.exec_id, this.suite_id, this.current_threshold,
					this.report_url,
					this.logs.toString()))
				this.temp = "[" + dtf.format(LocalDateTime.now()) + "] "
						+ "EXECUTION STATUS: Slack notification sent SUCCESSFULLY!" + "\n\n"
						+ this.temp;
			else
				this.temp = "[" + dtf.format(LocalDateTime.now()) + "] "
						+ "EXECUTION STATUS: Slack notification send FAILED!" + "\n\n"
						+ this.temp;

			System.out.print(this.temp);
			this.logs.append(this.temp);

			return false;
		}
	}
}

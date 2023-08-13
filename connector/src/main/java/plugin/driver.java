package plugin;

import java.io.IOException;
import java.text.ParseException;

import org.json.JSONException;
import org.json.JSONObject;

public class driver {

	public static void main(String[] args)
			throws InterruptedException {

		if (args.length < 1) {
			System.out.println(
					"ERROR: Execution Token is missing. Please enter a valid execution token and try again.\nExiting with a non-zero exit code.");
			System.out.println("EXECUTION FAILED!");
			System.exit(1);
		} else if (args.length > 1) {
			System.out.println(
					"ERROR: Please enter a valid number of arguments through the CLI i.e. only 1 JSON String.\nExiting with a non-zero exit code.");
			System.out.println("EXECUTION FAILED!");
			System.exit(1);
		} else {

			// Initializing Variables
			JSONObject pos_params = new JSONObject("{}");
			String exec_token = "";

			// Object construction for connector
			Connector sqa_connector = null;

			// Default values for the optional flags required for the execution
			String build_task_api = "https://simplifyqa.app/jenkinsSuiteExecution";
			String check_status_api = "https://simplifyqa.app/getJenkinsExecStatus";
			boolean response_flag = false;
			int poll_rate = 5, threshold = -1;

			try {
				pos_params = new JSONObject(args[0]);
			} catch (JSONException JE) {
				System.out.println(
						"ERROR: JSON String has some errors.\nPlease enter a JSON String through the CLI.\nExiting with a non-zero exit code.");
				System.out.println("EXECUTION FAILED!");
				JE.printStackTrace();
				System.exit(1);
			}

			if (pos_params.keySet().contains("exec_token")) {
				exec_token = pos_params.get("exec_token").toString();

				if (exec_token.equals("")) {
					System.out.println(
							"ERROR: Execution Token is missing. Please enter a valid execution token and try again.\nExiting with a non-zero exit code.");
					System.out.println("EXECUTION FAILED!");
					System.exit(1);
				}

			} else {
				System.out.println(
						"ERROR: Execution Token is missing. Please enter a valid execution token and try again.\nExiting with a non-zero exit code.");
				System.out.println("EXECUTION FAILED!");
				System.exit(1);
			}

			if (pos_params.keySet().contains("build_task_api"))
				build_task_api = pos_params.get("build_task_api").toString();

			if (pos_params.keySet().contains("build_task_api"))
				build_task_api = pos_params.get("build_task_api").toString();

			if (pos_params.keySet().contains("check_status_api"))
				check_status_api = pos_params.get("check_status_api").toString();

			if (pos_params.keySet().contains("response_flag"))
				response_flag = Boolean.parseBoolean(pos_params.get("response_flag").toString().toLowerCase());

			try {
				if (pos_params.keySet().contains("poll_rate"))
					poll_rate = Integer.parseInt(pos_params.get("poll_rate").toString());

				if (poll_rate < 0 && poll_rate > 60) {
					System.out.println(
							"WARNING: The parsed value for the field \"poll_rate\" can range from 0s to 60s.\nPlease parse a valid argument.\nResolving to default poll_rate i.e. 5s");
					poll_rate = 5;
				}

			} catch (NumberFormatException NFE) {
				System.out.println(
						"WARNING: The parsed value for the field \"poll_rate\" is not an integer.\nPlease parse a valid integer.\nResolving to default poll_rate i.e. 5s");
				// NFE.printStackTrace();
			}

			try {
				if (pos_params.keySet().contains("threshold"))
					threshold = Integer.parseInt(pos_params.get("threshold").toString());

				if (threshold < 1) {
					// basically the suite will fail even if all the testcases in the suite pass
					System.out.println(
							"WARNING: The parsed value for the field \"threshold\" cannot be equal to or less than 0.00%.\nPlease parse a valid argument.\nResolving to default threshold i.e. 1 testcase");
					threshold = -1;
				}

				if (threshold > 99) {
					// basically the suite will fail only when all the testcases in the suite fail
					System.out.println(
							"WARNING: The parsed value for the field \"threshold\" cannot be equal to or more than 100.00% .\nPlease parse a valid argument.\nResolving to default threshold i.e. 1 testcase");
					threshold = -1;
				}

			} catch (NumberFormatException NFE) {
				System.out.println(
						"WARNING: The parsed value for the field \"threshold\" is not an integer.\nPlease parse a valid integer.\nResolving to default threshold i.e. 1");
				threshold = -1;
				// NFE.printStackTrace();
			}

			try {
				System.out.println("\nThe Set Parameters are: \n");
				System.out.println("Execution Token: " + exec_token);
				System.out.println("Build Task API: " + build_task_api);
				System.out.println("Check Status API: " + check_status_api);
				System.out.println("Poll Rate: " + poll_rate);
				System.out.println("Response Body Flag: " + response_flag);
				if (threshold < 0)
					System.out.println("Threshold: 1 testcase");
				else
					System.out.println("Threshold: " + threshold + "%\n\n");

				Thread.sleep(3000);
				sqa_connector = new Connector(exec_token, build_task_api, check_status_api, poll_rate * 1000,
						threshold);
			} catch (IOException IOE) {
				IOE.printStackTrace();
			} catch (ParseException PE) {
				PE.printStackTrace();
			}

			try {
				if (sqa_connector.getExecInfo(response_flag)) {
					// System.out.println(sqa_connector.exec_info.toString());
					// System.out.println(sqa_connector.logs.toString());
					System.out.println("EXECUTION SUCCESS!");
					System.exit(0);
				} else {
					System.out.println("EXECUTION FAILED!");
					System.exit(1);
				}
			} catch (JSONException JE) {
				JE.printStackTrace();
			} catch (IOException IOE) {
				IOE.printStackTrace();
			}
		}
	}
}
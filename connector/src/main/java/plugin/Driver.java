package plugin;

import java.io.IOException;
import java.text.ParseException;

import org.json.JSONException;

public class Driver {

	public static void main(String[] args)
			throws InterruptedException {

		String exec_token = "";

		// Object construction for connector
		Executor sqa_connector = null;

		// Default values for the optional flags required for the execution
		String build_task_api = "https://qa.simplifyqa.app/jenkinsSuiteExecution";
		String check_status_api = "https://qa.simplifyqa.app/getJenkinsExecStatus";
		String kill_exec_api = "https://qa.simplifyqa.app/getsession/killExecutionReports";

		boolean request_flag = false;
		boolean response_flag = false;
		boolean verbose_flag = false;
		int poll_rate = 3, threshold = -1;

		// ARGUMENTS WARNING MESSAGES
		String invalid_build_api_msg = "\nWARNING: The parsed value for the field \"build_task_api\" is not a valid URL.\nPlease parse a valid URL.\nResolving to default value i.e. ";
		String invalid_status_api_msg = "\nWARNING: The parsed value for the field \"check_status_api\" is not a valid URL.\nPlease parse a valid URL.\nResolving to default value i.e. ";
		// String invalid_search_api_msg = "\nWARNING: The parsed value for the field
		// \"search_api\" is not a valid URL.\nPlease parse a valid URL.\nResolving to
		// default value i.e. ";
		// String poll_not_num_msg = "\nWARNING: The parsed value for the field
		// \"poll_rate\" is not an integer.\nPlease parse a valid integer.\nResolving to
		// default poll_rate i.e. 5s";
		String threshold_not_num_msg = "\nWARNING: The parsed value for the field \"threshold\" is not an integer.\nPlease parse a valid integer.\nResolving to default threshold i.e. 1";
		String threshold_less_than_0 = "\nWARNING: The parsed value for the field \"threshold\" cannot be equal to or less than 0.00%.\nPlease parse a valid argument.\nResolving to default threshold i.e. 1 testcase";
		String threshold_more_than_100 = "\nWARNING: The parsed value for the field \"threshold\" cannot be equal to or more than 100.00% .\nPlease parse a valid argument.\nResolving to default threshold i.e. 1 testcase";
		String invalid_killExec_msg = "\nWARNING: The parsed value for the field \"kill_exec_api\" is not a valid URL.\nPlease parse a valid URL.\nResolving to default value i.e. ";
		String invalid_slack_webhook_url = "\nWARNING: The parsed value for the field \"slack_webhook_url\" is not a valid URL.\nPlease parse a valid URL.\nFailed to export logs to Slack Channel.";

		// ARGUMENTS ERROR MESSAGES
		String missing_token_msg = "ERROR: Execution Token is missing. Please enter a valid execution token and try again.\nExiting with a non-zero exit code.";
		String invalid_token_msg = "ERROR: Execution Token is invalid. Please enter a valid execution token and try again.\nExiting with a non-zero exit code.";
		// String invalid_suite_id_msg = "\nWARNING: The parsed value for the field
		// \"suite_id\" is not a valid Suite ID.\nPlease parse a valid Suite ID and try
		// again.\nExiting with a non-zero exit code";

		switch (args.length) {

			case 0:
				//
				System.out.println(missing_token_msg);
				System.out.println("EXECUTION FAILED!");
				System.exit(1);
				break;

			case 1:
				// EXEC_TOKEN
				if (!args[0].equalsIgnoreCase("null"))
					exec_token = args[0];
				else {
					System.out.println(missing_token_msg);
					System.out.println("EXECUTION FAILED!");
					System.exit(1);
				}
				break;

			case 2:
				// EXEC_TOKEN, BUILD_API
				if (!args[0].equalsIgnoreCase("null"))
					exec_token = args[0];

				if (!args[1].equalsIgnoreCase("null")) {

					if (args[1].startsWith("http://") || args[1].startsWith("https://")
							|| args[1].split(".").length == 4)
						build_task_api = args[1];
					else {
						System.out.println(invalid_build_api_msg + build_task_api);
					}
				}

				break;

			case 3:
				// EXEC_TOKEN, BUILD_API, STATUS_API
				if (!args[0].equalsIgnoreCase("null"))
					exec_token = args[0];

				if (!args[1].equalsIgnoreCase("null")) {

					if (args[1].startsWith("http://") || args[1].startsWith("https://")
							|| args[1].split(".").length == 4)
						build_task_api = args[1];
					else {
						System.out.println(invalid_build_api_msg + build_task_api);
					}
				}

				if (!args[2].equalsIgnoreCase("null")) {
					if (args[2].startsWith("http://") || args[2].startsWith("https://")
							|| args[2].split(".").length == 4)
						check_status_api = args[2];
					else {
						System.out.println(invalid_status_api_msg + check_status_api);
					}
				}

				break;

			case 4:
				// EXEC_TOKEN, BUILD_API, STATUS_API, KILL_API
				if (!args[0].equalsIgnoreCase("null"))
					exec_token = args[0];

				if (!args[1].equalsIgnoreCase("null")) {

					if (args[1].startsWith("http://") || args[1].startsWith("https://")
							|| args[1].split(".").length == 4)
						build_task_api = args[1];
					else {
						System.out.println(invalid_build_api_msg + build_task_api);
					}
				}

				if (!args[2].equalsIgnoreCase("null")) {
					if (args[2].startsWith("http://") || args[2].startsWith("https://")
							|| args[2].split(".").length == 4)
						check_status_api = args[2];
					else {
						System.out.println(invalid_status_api_msg + check_status_api);
					}
				}

				if (!args[3].equalsIgnoreCase("null")) {
					if (args[3].startsWith("http://") || args[3].startsWith("https://")
							|| args[3].split(".").length == 4)
						kill_exec_api = args[3];
					else {
						System.out.println(invalid_killExec_msg + kill_exec_api);
					}
				}

				break;

			case 5:
				// EXEC_TOKEN, BUILD_API, STATUS_API, KILL_API, THRESHOLD
				if (!args[0].equalsIgnoreCase("null"))
					exec_token = args[0];

				if (!args[1].equalsIgnoreCase("null")) {

					if (args[1].startsWith("http://") || args[1].startsWith("https://")
							|| args[1].split(".").length == 4)
						build_task_api = args[1];
					else {
						System.out.println(invalid_build_api_msg + build_task_api);
					}
				}

				if (!args[2].equalsIgnoreCase("null")) {
					if (args[2].startsWith("http://") || args[2].startsWith("https://")
							|| args[2].split(".").length == 4)
						check_status_api = args[2];
					else {
						System.out.println(invalid_status_api_msg + check_status_api);
					}
				}

				if (!args[3].equalsIgnoreCase("null")) {
					if (args[3].startsWith("http://") || args[3].startsWith("https://")
							|| args[3].split(".").length == 4)
						kill_exec_api = args[3];
					else {
						System.out.println(invalid_killExec_msg + kill_exec_api);
					}
				}

				try {
					if (!args[4].equalsIgnoreCase("null"))
						threshold = Integer.parseInt(args[4]);
				} catch (NumberFormatException NFE) {
					System.out.println(threshold_not_num_msg);
					threshold = -1;
					// NFE.printStackTrace();
				}

				break;

			case 6:
				// EXEC_TOKEN, BUILD_API, STATUS_API, KILL_API, THRESHOLD, VERBOSE
				if (!args[0].equalsIgnoreCase("null"))
					exec_token = args[0];

				if (!args[1].equalsIgnoreCase("null")) {

					if (args[1].startsWith("http://") || args[1].startsWith("https://")
							|| args[1].split(".").length == 4)
						build_task_api = args[1];
					else {
						System.out.println(invalid_build_api_msg + build_task_api);
					}
				}

				if (!args[2].equalsIgnoreCase("null")) {
					if (args[2].startsWith("http://") || args[2].startsWith("https://")
							|| args[2].split(".").length == 4)
						check_status_api = args[2];
					else {
						System.out.println(invalid_status_api_msg + check_status_api);
					}
				}

				if (!args[3].equalsIgnoreCase("null")) {
					if (args[3].startsWith("http://") || args[3].startsWith("https://")
							|| args[3].split(".").length == 4)
						kill_exec_api = args[3];
					else {
						System.out.println(invalid_killExec_msg + kill_exec_api);
					}
				}

				try {
					if (!args[4].equalsIgnoreCase("null"))
						threshold = Integer.parseInt(args[4]);
				} catch (NumberFormatException NFE) {
					System.out.println(threshold_not_num_msg);
					threshold = -1;
					// NFE.printStackTrace();
				}

				if (!args[5].equalsIgnoreCase("null"))
					verbose_flag = Boolean.parseBoolean(args[5]);

				break;

			case 7:
				// EXEC_TOKEN, BUILD_API, STATUS_API, KILL_API, THRESHOLD, VERBOSE,
				// SLACK_WEBHOOK
				if (!args[0].equalsIgnoreCase("null"))
					exec_token = args[0];

				if (!args[1].equalsIgnoreCase("null")) {

					if (args[1].startsWith("http://") || args[1].startsWith("https://")
							|| args[1].split(".").length == 4)
						build_task_api = args[1];
					else {
						System.out.println(invalid_build_api_msg + build_task_api);
					}
				}

				if (!args[2].equalsIgnoreCase("null")) {
					if (args[2].startsWith("http://") || args[2].startsWith("https://")
							|| args[2].split(".").length == 4)
						check_status_api = args[2];
					else {
						System.out.println(invalid_status_api_msg + check_status_api);
					}
				}

				if (!args[3].equalsIgnoreCase("null")) {
					if (args[3].startsWith("http://") || args[3].startsWith("https://")
							|| args[3].split(".").length == 4)
						kill_exec_api = args[3];
					else {
						System.out.println(invalid_killExec_msg + kill_exec_api);
					}
				}

				try {
					if (!args[4].equalsIgnoreCase("null"))
						threshold = Integer.parseInt(args[4]);
				} catch (NumberFormatException NFE) {
					System.out.println(threshold_not_num_msg);
					threshold = -1;
					// NFE.printStackTrace();
				}

				if (!args[5].equalsIgnoreCase("null"))
					verbose_flag = Boolean.parseBoolean(args[5]);

				if (!args[6].equalsIgnoreCase("null")) {
					if (args[6].startsWith("http://") || args[6].startsWith("https://")
							|| args[6].split(".").length == 4)
						Slackbot.webhook_url = args[6];
					else {
						System.out.println(invalid_slack_webhook_url + Slackbot.webhook_url);
					}
				}

				break;

			default:

				System.out.println(
						"ERROR: Please enter a valid number of parameters and try again. No. of parameters entered"
								+ args.length + "\n Entered parameters are: \n");

				for (int i = 0; i < args.length; i--) {
					System.out.println("\nArgument no. " + i + ": " + args[i]);
				}

				System.out.println("\nExiting with a non-zero exit code.");
				System.out.println("EXECUTION FAILED!");
				System.exit(1);
				break;
		}

		if (verbose_flag) {
			request_flag = true;
			response_flag = true;
		}

		if (exec_token.equals("")) {
			System.out.println(missing_token_msg);
			System.out.println("EXECUTION FAILED!");
			System.exit(1);
		}

		if (exec_token.length() != 88) {
			System.out.println(invalid_token_msg);
			System.out.println("EXECUTION FAILED!");
			System.exit(1);
		}

		if (threshold == 0) {
			// basically the suite will fail even if all the testcases in the suite pass
			System.out.println(
					threshold_less_than_0);
			threshold = -1;
		}

		if (threshold == 100) {
			// basically the suite will fail only when all the testcases in the suite fail
			System.out.println(
					threshold_more_than_100);
			threshold = -1;
		}

		try {
			System.out.println("\nThe Set Parameters are: \n");
			System.out.println(
					"Execution Token: *****************************************" + exec_token.substring(82, 88));
			System.out.println("Build Task API: " + build_task_api);
			System.out.println("Check Status API: " + check_status_api);
			// System.out.println("Search API: " + search_api);
			System.out.println("Kill Execution API: " + kill_exec_api);
			System.out.println("Poll Rate: " + poll_rate);
			System.out.println("Request Body Flag: " + request_flag);
			System.out.println("Response Body Flag: " + response_flag);
			if (threshold < 0)
				System.out.println("Threshold: 1 testcase");
			else
				System.out.println("Threshold: " + threshold + "%");

			Thread.sleep(3000);
			sqa_connector = new Executor(exec_token, build_task_api, check_status_api, kill_exec_api,
					poll_rate * 1000,
					threshold);
		} catch (IOException IOE) {
			IOE.printStackTrace();
		} catch (ParseException PE) {
			PE.printStackTrace();
		}

		try {
			if (sqa_connector.getExecInfo(request_flag, response_flag)) {
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

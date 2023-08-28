package plugin;

import java.io.IOException;
import java.text.ParseException;

import org.json.JSONException;

public class Driver {

	public static void main(String[] args)
			throws InterruptedException {

		// Initializing Variables
		// JSONObject pos_params = new JSONObject("{}");
		// BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		// String exec_token = br.readLine();
		// br.close();
		String exec_token = "";

		// Object construction for connector
		Executor sqa_connector = null;

		// Default values for the optional flags required for the execution
		String build_task_api = "https://simplifyqa.app/jenkinsSuiteExecution";
		String check_status_api = "https://simplifyqa.app/getJenkinsExecStatus";
		String search_api = "https://simplifyqa.app/search";
		String kill_exec_api = "https://simplifyqa.app/getsession/killExecutionReports";

		boolean request_flag = false;
		boolean response_flag = false;
		int poll_rate = 5, threshold = -1;
		String suite_id = null;

		// ARGUMENTS WARNING MESSAGES
		String invalid_build_api_msg = "\nWARNING: The parsed value for the field \"build_task_api\" is not a valid URL.\nPlease parse a valid URL.\nResolving to default value i.e. ";
		String invalid_status_api_msg = "\nWARNING: The parsed value for the field \"check_status_api\" is not a valid URL.\nPlease parse a valid URL.\nResolving to default value i.e. ";
		String invalid_search_api_msg = "\nWARNING: The parsed value for the field \"search_api\" is not a valid URL.\nPlease parse a valid URL.\nResolving to default value i.e. ";
		String poll_not_num_msg = "\nWARNING: The parsed value for the field \"poll_rate\" is not an integer.\nPlease parse a valid integer.\nResolving to default poll_rate i.e. 5s";
		String threshold_not_num_msg = "\nWARNING: The parsed value for the field \"threshold\" is not an integer.\nPlease parse a valid integer.\nResolving to default threshold i.e. 1";
		String threshold_less_than_0 = "\nWARNING: The parsed value for the field \"threshold\" cannot be equal to or less than 0.00%.\nPlease parse a valid argument.\nResolving to default threshold i.e. 1 testcase";
		String threshold_more_than_100 = "\nWARNING: The parsed value for the field \"threshold\" cannot be equal to or more than 100.00% .\nPlease parse a valid argument.\nResolving to default threshold i.e. 1 testcase";
		String invalid_killExec_msg = "\nWARNING: The parsed value for the field \"kill_exec_api\" is not a valid URL.\nPlease parse a valid URL.\nResolving to default value i.e. ";
		String invalid_slack_webhook_url = "\nWARNING: The parsed value for the field \"slack_webhook_url\" is not a valid URL.\nPlease parse a valid URL.\nFailed to export logs to Slack Channel.";

		// ARGUMENTS ERROR MESSAGES
		String missing_token_msg = "ERROR: Execution Token is missing. Please enter a valid execution token and try again.\nExiting with a non-zero exit code.";
		String invalid_suite_id_msg = "\nWARNING: The parsed value for the field \"suite_id\" is not a valid Suite ID.\nPlease parse a valid Suite ID and try again.\nExiting with a non-zero exit code";

		// if (args.length < 1) {

		// } else if (args.length > 1) {
		// System.out.println(
		// "ERROR: Please enter a valid number of arguments through the CLI i.e. only 1
		// JSON String.\nExiting with a non-zero exit code.");
		// System.out.println("EXECUTION FAILED!");
		// System.exit(1);
		// } else {

		// try {
		// pos_params = new JSONObject(args[0]);
		// } catch (JSONException JE) {

		// System.out.println(
		// "ERROR: JSON String has some errors.\nPlease enter a JSON String through the
		// CLI.\nExiting with a non-zero exit code.");
		// System.out.println("EXECUTION FAILED!");
		// JE.printStackTrace();
		// System.exit(1);
		// }

		// if (pos_params.keySet().contains("exec_token")) {
		// exec_token = pos_params.get("exec_token").toString();

		// if (exec_token.equals("")) {
		// System.out.println(
		// "ERROR: Execution Token is missing. Please enter a valid execution token and
		// try again.\nExiting with a non-zero exit code.");
		// System.out.println("EXECUTION FAILED!");
		// System.exit(1);
		// }

		// } else {
		// System.out.println(
		// "ERROR: Execution Token is missing. Please enter a valid execution token and
		// try again.\nExiting with a non-zero exit code.");
		// System.out.println("EXECUTION FAILED!");
		// System.exit(1);
		// }

		// if (pos_params.keySet().contains("build_task_api"))
		// build_task_api = pos_params.get("build_task_api").toString();

		// if (pos_params.keySet().contains("build_task_api"))
		// build_task_api = pos_params.get("build_task_api").toString();

		// if (pos_params.keySet().contains("check_status_api"))
		// check_status_api = pos_params.get("check_status_api").toString();

		// if (pos_params.keySet().contains("response_flag"))
		// response_flag =
		// Boolean.parseBoolean(pos_params.get("response_flag").toString().toLowerCase());

		// try {
		// if (pos_params.keySet().contains("poll_rate"))
		// poll_rate = Integer.parseInt(pos_params.get("poll_rate").toString());

		// if (poll_rate < 0 && poll_rate > 60) {
		// System.out.println(
		// "\nWARNING: The parsed value for the field \"poll_rate\" can range from 0s to
		// 60s.\nPlease parse a valid argument.\nResolving to default poll_rate i.e.
		// 5s");
		// poll_rate = 5;
		// }

		// } catch (NumberFormatException NFE) {
		// System.out.println(
		// "\nWARNING: The parsed value for the field \"poll_rate\" is not an
		// integer.\nPlease parse a valid integer.\nResolving to default poll_rate i.e.
		// 5s");
		// // NFE.printStackTrace();
		// }

		// try {
		// if (pos_params.keySet().contains("threshold"))
		// threshold = Integer.parseInt(pos_params.get("threshold").toString());

		// if (threshold < 1) {
		// // basically the suite will fail even if all the testcases in the suite pass
		// System.out.println(
		// "\nWARNING: The parsed value for the field \"threshold\" cannot be equal to
		// or
		// less than 0.00%.\nPlease parse a valid argument.\nResolving to default
		// threshold i.e. 1 testcase");
		// threshold = -1;
		// }

		// if (threshold > 99) {
		// // basically the suite will fail only when all the testcases in the suite
		// fail
		// System.out.println(
		// "\nWARNING: The parsed value for the field \"threshold\" cannot be equal to
		// or
		// more than 100.00% .\nPlease parse a valid argument.\nResolving to default
		// threshold i.e. 1 testcase");
		// threshold = -1;
		// }

		// } catch (NumberFormatException NFE) {
		// System.out.println(
		// "\nWARNING: The parsed value for the field \"threshold\" is not an
		// integer.\nPlease parse a valid integer.\nResolving to default threshold i.e.
		// 1");
		// threshold = -1;
		// // NFE.printStackTrace();
		// }

		switch (args.length) {

			case 0:
				System.out.println(missing_token_msg);
				System.out.println("EXECUTION FAILED!");
				System.exit(1);
				break;

			case 1:
				if (!args[0].equals("null"))
					exec_token = args[0];
				else {
					System.out.println(missing_token_msg);
					System.out.println("EXECUTION FAILED!");
					System.exit(1);
				}
				break;

			case 2:
				if (!args[0].equals("null"))
					exec_token = args[0];

				if (!args[1].equals("null")) {

					if (args[1].startsWith("http://") || args[1].startsWith("https://")
							|| args[1].split(".").length == 4)
						build_task_api = args[1];
					else {
						System.out.println(invalid_build_api_msg + build_task_api);
					}
				}

				break;

			case 3:
				if (!args[0].equals("null"))
					exec_token = args[0];

				if (!args[1].equals("null")) {

					if (args[1].startsWith("http://") || args[1].startsWith("https://")
							|| args[1].split(".").length == 4)
						build_task_api = args[1];
					else {
						System.out.println(invalid_build_api_msg + build_task_api);
					}
				}

				if (!args[2].equals("null")) {
					if (args[2].startsWith("http://") || args[2].startsWith("https://")
							|| args[2].split(".").length == 4)
						check_status_api = args[2];
					else {
						System.out.println(invalid_status_api_msg + check_status_api);
					}
				}

				break;

			case 4:
				if (!args[0].equals("null"))
					exec_token = args[0];

				if (!args[1].equals("null")) {

					if (args[1].startsWith("http://") || args[1].startsWith("https://")
							|| args[1].split(".").length == 4)
						build_task_api = args[1];
					else {
						System.out.println(invalid_build_api_msg + build_task_api);
					}
				}

				if (!args[2].equals("null")) {
					if (args[2].startsWith("http://") || args[2].startsWith("https://")
							|| args[2].split(".").length == 4)
						check_status_api = args[2];
					else {
						System.out.println(invalid_status_api_msg + check_status_api);
					}
				}

				if (!args[3].equals("null")) {
					if (args[3].startsWith("http://") || args[3].startsWith("https://")
							|| args[3].split(".").length == 4)
						search_api = args[3];
					else {
						System.out.println(invalid_search_api_msg + search_api);
					}
				}

				break;

			case 5:
				if (!args[0].equals("null"))
					exec_token = args[0];

				if (!args[1].equals("null")) {

					if (args[1].startsWith("http://") || args[1].startsWith("https://")
							|| args[1].split(".").length == 4)
						build_task_api = args[1];
					else {
						System.out.println(invalid_build_api_msg + build_task_api);
					}
				}

				if (!args[2].equals("null")) {
					if (args[2].startsWith("http://") || args[2].startsWith("https://")
							|| args[2].split(".").length == 4)
						check_status_api = args[2];
					else {
						System.out.println(invalid_status_api_msg + check_status_api);
					}
				}

				if (!args[3].equals("null")) {
					if (args[3].startsWith("http://") || args[3].startsWith("https://")
							|| args[3].split(".").length == 4)
						search_api = args[3];
					else {
						System.out.println(invalid_search_api_msg + search_api);
					}
				}

				if (!args[4].equals("null"))
					request_flag = Boolean.parseBoolean(args[4]);

				break;

			case 6:
				if (!args[0].equals("null"))
					exec_token = args[0];

				if (!args[1].equals("null")) {

					if (args[1].startsWith("http://") || args[1].startsWith("https://")
							|| args[1].split(".").length == 4)
						build_task_api = args[1];
					else {
						System.out.println(invalid_build_api_msg + build_task_api);
					}
				}

				if (!args[2].equals("null")) {
					if (args[2].startsWith("http://") || args[2].startsWith("https://")
							|| args[2].split(".").length == 4)
						check_status_api = args[2];
					else {
						System.out.println(invalid_status_api_msg + check_status_api);
					}
				}

				if (!args[3].equals("null")) {
					if (args[3].startsWith("http://") || args[3].startsWith("https://")
							|| args[3].split(".").length == 4)
						search_api = args[3];
					else {
						System.out.println(invalid_search_api_msg + search_api);
					}
				}

				if (!args[4].equals("null"))
					request_flag = Boolean.parseBoolean(args[4]);

				if (!args[5].equals("null"))
					response_flag = Boolean.parseBoolean(args[5]);

				break;

			case 7:
				if (!args[0].equals("null"))
					exec_token = args[0];

				if (!args[1].equals("null")) {

					if (args[1].startsWith("http://") || args[1].startsWith("https://")
							|| args[1].split(".").length == 4)
						build_task_api = args[1];
					else {
						System.out.println(invalid_build_api_msg + build_task_api);
					}
				}

				if (!args[2].equals("null")) {
					if (args[2].startsWith("http://") || args[2].startsWith("https://")
							|| args[2].split(".").length == 4)
						check_status_api = args[2];
					else {
						System.out.println(invalid_status_api_msg + check_status_api);
					}
				}

				if (!args[3].equals("null")) {
					if (args[3].startsWith("http://") || args[3].startsWith("https://")
							|| args[3].split(".").length == 4)
						search_api = args[3];
					else {
						System.out.println(invalid_search_api_msg + search_api);
					}
				}

				if (!args[4].equals("null"))
					request_flag = Boolean.parseBoolean(args[4]);

				if (!args[5].equals("null"))
					response_flag = Boolean.parseBoolean(args[5]);

				try {
					if (!args[6].equals("null"))
						poll_rate = Integer.parseInt(args[6]);
				} catch (NumberFormatException NFE) {
					System.out.println(poll_not_num_msg);
					// NFE.printStackTrace();
				}

				break;

			case 8:
				if (!args[0].equals("null"))
					exec_token = args[0];

				if (!args[1].equals("null")) {

					if (args[1].startsWith("http://") || args[1].startsWith("https://")
							|| args[1].split(".").length == 4)
						build_task_api = args[1];
					else {
						System.out.println(invalid_build_api_msg + build_task_api);
					}
				}

				if (!args[2].equals("null")) {
					if (args[2].startsWith("http://") || args[2].startsWith("https://")
							|| args[2].split(".").length == 4)
						check_status_api = args[2];
					else {
						System.out.println(invalid_status_api_msg + check_status_api);
					}
				}

				if (!args[3].equals("null")) {
					if (args[3].startsWith("http://") || args[3].startsWith("https://")
							|| args[3].split(".").length == 4)
						search_api = args[3];
					else {
						System.out.println(invalid_search_api_msg + search_api);
					}
				}

				if (!args[4].equals("null"))
					request_flag = Boolean.parseBoolean(args[4]);

				if (!args[5].equals("null"))
					response_flag = Boolean.parseBoolean(args[5]);

				try {
					if (!args[6].equals("null"))
						poll_rate = Integer.parseInt(args[6]);
				} catch (NumberFormatException NFE) {
					System.out.println(poll_not_num_msg);
					// NFE.printStackTrace();
				}

				try {
					if (!args[7].equals("null"))
						threshold = Integer.parseInt(args[7]);
				} catch (NumberFormatException NFE) {
					System.out.println(threshold_not_num_msg);
					threshold = -1;
					// NFE.printStackTrace();
				}
				break;

			case 9:
				if (!args[0].equals("null"))
					exec_token = args[0];

				if (!args[1].equals("null")) {

					if (args[1].startsWith("http://") || args[1].startsWith("https://")
							|| args[1].split(".").length == 4)
						build_task_api = args[1];
					else {
						System.out.println(invalid_build_api_msg + build_task_api);
					}
				}

				if (!args[2].equals("null")) {
					if (args[2].startsWith("http://") || args[2].startsWith("https://")
							|| args[2].split(".").length == 4)
						check_status_api = args[2];
					else {
						System.out.println(invalid_status_api_msg + check_status_api);
					}
				}

				if (!args[3].equals("null")) {
					if (args[3].startsWith("http://") || args[2].startsWith("https://")
							|| args[3].split(".").length == 4)
						search_api = args[3];
					else {
						System.out.println(invalid_status_api_msg + check_status_api);
					}
				}

				if (!args[4].equals("null"))
					request_flag = Boolean.parseBoolean(args[4]);

				if (!args[5].equals("null"))
					response_flag = Boolean.parseBoolean(args[5]);

				try {
					if (!args[6].equals("null"))
						poll_rate = Integer.parseInt(args[6]);
				} catch (NumberFormatException NFE) {
					System.out.println(poll_not_num_msg);
					// NFE.printStackTrace();
				}

				try {
					if (!args[7].equals("null"))
						threshold = Integer.parseInt(args[7]);
				} catch (NumberFormatException NFE) {
					System.out.println(threshold_not_num_msg);
					threshold = -1;
					// NFE.printStackTrace();
				}

				if (!args[8].equals("null")) {

					if (args[8].toLowerCase().startsWith("su-"))
						suite_id = args[8];
					else {
						System.out.println(invalid_suite_id_msg);
						System.out.println("EXECUTION FAILED!");
						System.exit(1);
					}

				} else {
					System.out.println(invalid_suite_id_msg);
					System.out.println("EXECUTION FAILED!");
					System.exit(1);
				}

				break;

			case 10:
				if (!args[0].equals("null"))
					exec_token = args[0];

				if (!args[1].equals("null")) {

					if (args[1].startsWith("http://") || args[1].startsWith("https://")
							|| args[1].split(".").length == 4)
						build_task_api = args[1];
					else {
						System.out.println(invalid_build_api_msg + build_task_api);
					}
				}

				if (!args[2].equals("null")) {
					if (args[2].startsWith("http://") || args[2].startsWith("https://")
							|| args[2].split(".").length == 4)
						check_status_api = args[2];
					else {
						System.out.println(invalid_status_api_msg + check_status_api);
					}
				}

				if (!args[3].equals("null")) {
					if (args[3].startsWith("http://") || args[2].startsWith("https://")
							|| args[3].split(".").length == 4)
						search_api = args[3];
					else {
						System.out.println(invalid_status_api_msg + check_status_api);
					}
				}

				if (!args[4].equals("null"))
					request_flag = Boolean.parseBoolean(args[4]);

				if (!args[5].equals("null"))
					response_flag = Boolean.parseBoolean(args[5]);

				try {
					if (!args[6].equals("null"))
						poll_rate = Integer.parseInt(args[6]);
				} catch (NumberFormatException NFE) {
					System.out.println(poll_not_num_msg);
					// NFE.printStackTrace();
				}

				try {
					if (!args[7].equals("null"))
						threshold = Integer.parseInt(args[7]);
				} catch (NumberFormatException NFE) {
					System.out.println(threshold_not_num_msg);
					threshold = -1;
					// NFE.printStackTrace();
				}

				if (!args[8].equals("null")) {

					if (args[8].toLowerCase().startsWith("su-"))
						suite_id = args[8];
					else {
						System.out.println(invalid_suite_id_msg);
						System.out.println("EXECUTION FAILED!");
						System.exit(1);
					}

				} else {
					System.out.println(invalid_suite_id_msg);
					System.out.println("EXECUTION FAILED!");
					System.exit(1);
				}

				if (!args[9].equals("null")) {
					if (args[9].startsWith("http://") || args[9].startsWith("https://")
							|| args[9].split(".").length == 4)
						kill_exec_api = args[9];
					else {
						System.out.println(invalid_killExec_msg + kill_exec_api);
					}
				}

				break;

			case 11:
				if (!args[0].equals("null"))
					exec_token = args[0];

				if (!args[1].equals("null")) {

					if (args[1].startsWith("http://") || args[1].startsWith("https://")
							|| args[1].split(".").length == 4)
						build_task_api = args[1];
					else {
						System.out.println(invalid_build_api_msg + build_task_api);
					}
				}

				if (!args[2].equals("null")) {
					if (args[2].startsWith("http://") || args[2].startsWith("https://")
							|| args[2].split(".").length == 4)
						check_status_api = args[2];
					else {
						System.out.println(invalid_status_api_msg + check_status_api);
					}
				}

				if (!args[3].equals("null")) {
					if (args[3].startsWith("http://") || args[2].startsWith("https://")
							|| args[3].split(".").length == 4)
						search_api = args[3];
					else {
						System.out.println(invalid_status_api_msg + check_status_api);
					}
				}

				if (!args[4].equals("null"))
					request_flag = Boolean.parseBoolean(args[4]);

				if (!args[5].equals("null"))
					response_flag = Boolean.parseBoolean(args[5]);

				try {
					if (!args[6].equals("null"))
						poll_rate = Integer.parseInt(args[6]);
				} catch (NumberFormatException NFE) {
					System.out.println(poll_not_num_msg);
					// NFE.printStackTrace();
				}

				try {
					if (!args[7].equals("null"))
						threshold = Integer.parseInt(args[7]);
				} catch (NumberFormatException NFE) {
					System.out.println(threshold_not_num_msg);
					threshold = -1;
					// NFE.printStackTrace();
				}

				if (!args[8].equals("null")) {

					if (args[8].toLowerCase().startsWith("su-"))
						suite_id = args[8];
					else {
						System.out.println(invalid_suite_id_msg);
						System.out.println("EXECUTION FAILED!");
						System.exit(1);
					}

				} else {
					System.out.println(invalid_suite_id_msg);
					System.out.println("EXECUTION FAILED!");
					System.exit(1);
				}

				if (!args[9].equals("null")) {
					if (args[9].startsWith("http://") || args[9].startsWith("https://")
							|| args[9].split(".").length == 4)
						kill_exec_api = args[9];
					else {
						System.out.println(invalid_killExec_msg + kill_exec_api);
					}
				}

				if (!args[10].equals("null")) {
					if (args[10].startsWith("http://") || args[10].startsWith("https://")
							|| args[10].split(".").length == 4)
						Slackbot.webhook_url = args[10];
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

		if (exec_token.equals(""))

		{
			System.out.println(missing_token_msg);
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
			System.out.println("Execution Token: " + exec_token);
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
			// System.out.println("Suite ID: " + suite_id + "\n");

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

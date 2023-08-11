package plugin;

import java.io.IOException;
import java.text.ParseException;

public class driver {
	public static void main(String[] args) throws IOException, InterruptedException, ParseException, ParseException {

		String build_task_api = args[0];
		String check_status_api = args[1];
		String token = args[2];
		boolean response_flag = Boolean.parseBoolean(args[3].toLowerCase());
		int poll_rate = Integer.parseInt(args[4]);

		// Object construction for connector
		Connector sqa_connector = new Connector(token, build_task_api, check_status_api, poll_rate * 1000);
		if (sqa_connector.getExecInfo(response_flag)) {
			// System.out.println(sqa_connector.exec_info.toString());
			// System.out.println(sqa_connector.logs.toString());
			System.out.println("EXECUTION SUCCESS!");
			System.exit(0);
		} else {
			System.out.println("EXECUTION FAILED!");
			System.exit(1);
		}
	}
}

package plugin;

import java.io.IOException;
import java.text.ParseException;

public class driver {
	public static void main(String[] args) throws IOException, InterruptedException, ParseException, ParseException {

		String build_task_api = args[0];
		String check_status_api = args[1];
		String token = args[2];

		// Object construction for connector
		Connector sqa_connector = new Connector(token, build_task_api, check_status_api);
		if (sqa_connector.getExecInfo()) {
			// System.out.println(sqa_connector.exec_info.toString());
			System.out.println(sqa_connector.logs.toString());
			System.out.print("EXECUTION SUCCESS!");
		} else {
			System.out.println("EXECUTION FAILED!");
		}
	}
}

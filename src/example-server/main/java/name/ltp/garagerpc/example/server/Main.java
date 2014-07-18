package name.ltp.garagerpc.example.server;

import name.ltp.garagerpc.server.webbit.WebbitRpcHandler;
import name.ltp.garagerpc.server.webbit.WebbitRpcServer;
import org.webbitserver.handler.StaticFileHandler;

public class Main
{
	public static void main(String args[]) throws Exception
	{
		WebbitRpcServer w = new WebbitRpcServer(31337);

		w
			.add(new StaticFileHandler("./out/gwt-example/war")) // index.html and GWT output.
			.add(new WebbitRpcHandler(RExampleService.class))
			;

		w
			.start()
			.get();
	}
}

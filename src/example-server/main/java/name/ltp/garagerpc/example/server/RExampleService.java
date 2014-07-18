package name.ltp.garagerpc.example.server;

import name.ltp.garagerpc.server.webbit.WebbitRpcContext;
import name.ltp.garagerpc.shared.c;

public class RExampleService implements name.ltp.garagerpc.example.shared.RExampleService
{
	@Override
	public String greet(String name, c<String, WebbitRpcContext> co)
	{
		return
			  "Hello, " + name + "!"
			+ " This greeting was generated on a server and "
			+ " you seem to be using '" + co.serverContext.request.header("User-Agent") + "'.";
	}
}

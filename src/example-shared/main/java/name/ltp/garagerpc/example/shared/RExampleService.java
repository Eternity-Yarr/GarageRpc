package name.ltp.garagerpc.example.shared;

import name.ltp.garagerpc.server.webbit.WebbitRpcContext;
import name.ltp.garagerpc.shared.RpcPath;
import name.ltp.garagerpc.shared.RpcService;
import name.ltp.garagerpc.shared.c;

@RpcPath("/rpc/exampleservice")
public interface RExampleService extends RpcService
{
	String greet(String name, c<String, WebbitRpcContext> co);
}

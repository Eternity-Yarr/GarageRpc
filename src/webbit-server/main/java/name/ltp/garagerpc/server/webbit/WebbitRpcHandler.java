package name.ltp.garagerpc.server.webbit;

import name.ltp.garagerpc.server.Err;
import name.ltp.garagerpc.server.RpcHandler;
import name.ltp.garagerpc.shared.RpcService;
import name.ltp.garagerpc.shared.c;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import java.util.Date;

public class WebbitRpcHandler extends RpcHandler implements HttpHandler
{
	public WebbitRpcHandler(Class<? extends RpcService> rpcservice) throws Exception
	{
		super(rpcservice);
	}

	@Override
	public void handleHttpRequest(final HttpRequest request, final HttpResponse response, final HttpControl control)
	{
		if(!request.method().equalsIgnoreCase("POST"))
		{
			Err.i().err("GarageRpc: Request method != POST. Request URI: " + request.uri());

			control.nextHandler();

			return;
		}
		else if(!request.header("Content-Type").contains("application/json"))
		{
			Err.i().err("GarageRpc: Request content-type != application/json (" + request.header("Content-Type") + "). Request URI: " + request.uri());

			control.nextHandler();

			return;
		}

		response.header("Content-Type", "application/json; charset=UTF-8");

		@SuppressWarnings("unchecked")
		final c<?, WebbitRpcContext> co = new c(new WebbitRpcContext(request, response, control));

		exec(co, request.body().substring(12));
	}

	// Override to handle request in another thread.
	public void exec(c<?, WebbitRpcContext> co, String rpcId)
	{
		final String r = handle(co, rpcId, co.serverContext.request.body());

		co.serverContext.response
			.header("Cache-control", "no-cache, no-store, must-revalidate, proxy-revalidate, max-age=0, s-maxage=0")
			.header("Last-Modified", new Date())
			.header("Expires", new Date(0))
			.header("Pragma", "no-cache")
			.content(r)
			;

		end(co);
	}

	public void end(c<?, WebbitRpcContext> co)
	{
		co.serverContext.response.end();
	}
}

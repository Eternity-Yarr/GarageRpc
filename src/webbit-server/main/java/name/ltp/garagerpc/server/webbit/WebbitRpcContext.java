package name.ltp.garagerpc.server.webbit;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

public class WebbitRpcContext
{
	public final HttpRequest request;
	public final HttpResponse response;
	public final HttpControl control;

	public WebbitRpcContext(HttpRequest request, HttpResponse response, HttpControl control)
	{
		this.request = request;
		this.response = response;
		this.control = control;
	}
}

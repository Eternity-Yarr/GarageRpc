package name.ltp.garagerpc.server.webbit;

import org.webbitserver.handler.PathMatchHandler;
import org.webbitserver.netty.NettyWebServer;

import java.net.SocketAddress;
import java.net.URI;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public class WebbitRpcServer extends NettyWebServer
{
	//
	public WebbitRpcServer(int port)
	{
		super(port);
	}

	private WebbitRpcServer(ExecutorService executorService, int port)
	{
		super(executorService, port);
	}

	public WebbitRpcServer(final Executor executor, int port)
	{
		super(executor, port);
	}

	public WebbitRpcServer(final Executor executor, SocketAddress socketAddress, URI publicUri)
	{
		super(executor, socketAddress, publicUri);
	}

	//
	public WebbitRpcServer add(WebbitRpcHandler handler)
	{
		add(new PathMatchHandler(handler.rpcpath + "(\\z|/.*)", handler));

		return this;
	}
}

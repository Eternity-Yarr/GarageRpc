GarageRpc - Custom GWT RPC mechanism.

## Example
The full example is in `src/example-*`.


Code shared with client and server: [shared/RExampleService.java](https://github.com/lostdj/GarageRpc/blob/master/src/example-shared/main/java/name/ltp/garagerpc/example/shared/RExampleService.java)
```java
@RpcPath("/rpc/exampleservice")
public interface RExampleService extends RpcService
{
	String greet(String name, c<String, WebbitRpcContext> co);
}
```


Server implementation: [server/RExampleService.java](https://github.com/lostdj/GarageRpc/blob/master/src/example-server/main/java/name/ltp/garagerpc/example/server/RExampleService.java)
```java
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
```


[Webbit](https://github.com/webbit/webbit) is used for this example: [server/Main.java](https://github.com/lostdj/GarageRpc/blob/master/src/example-server/main/java/name/ltp/garagerpc/example/server/Main.java)
```java
new WebbitRpcServer(31337)
	.add(new StaticFileHandler("./out/gwt-example/war")) // index.html and GWT output.
	.add(new WebbitRpcHandler(RExampleService.class)) // Server RPC impl.
	.start()
	.get(); // Loop.
```


Client: [client/Main.java](https://github.com/lostdj/GarageRpc/blob/master/src/example-client/main/java/name/ltp/garagerpc/example/client/Main.java)
```java
public class Main implements EntryPoint
{
	static final RExampleService rpc = GWT.create(RExampleService.class);

	@Override
	public void onModuleLoad()
	{
		rpc.greet(
			"User Name the 3rd",
			new c<String, WebbitRpcContext>()
			{
				@Override
				public void s() // Success.
				{
					if(error == null)
						$("body").text(v);
				}

				@Override
				public void f() // Failure.
				{
					$("body").text("Error");
				}
			});
	}
}
```

## Handlers
GRPC doesn't depend on any web server and you are free to implement your own handler for a web server of your choice.

Take a look on implementation for [Webbit](https://github.com/webbit/webbit).

* Handler itself [src/webbit-server/.../WebbitRpcHandler.java](https://github.com/lostdj/GarageRpc/blob/master/src/webbit-server/main/java/name/ltp/garagerpc/server/webbit/WebbitRpcHandler.java).

* Subclass of the Webbit server [src/webbit-server/.../WebbitRpcServer.java](https://github.com/lostdj/GarageRpc/blob/master/src/webbit-server/main/java/name/ltp/garagerpc/server/webbit/WebbitRpcServer.java):
```java
public class WebbitRpcServer extends NettyWebServer
{
	// ...

	// Add RPC request handler.
	public WebbitRpcServer add(WebbitRpcHandler handler)
	{
		add(new PathMatchHandler(handler.rpcpath + "(\\z|/.*)", handler));

		return this;
	}
}
```

* Request context [src/webbit-server/.../WebbitRpcContext.java](https://github.com/lostdj/GarageRpc/blob/master/src/webbit-server/main/java/name/ltp/garagerpc/server/webbit/WebbitRpcContext.java):
```java
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
```

* And a dummy request context just to trick a client's compiler, so it won't complain on unknown WebbitRpcContext class in shared source code (shared/RExampleService.java interface in the example above) [src/webbit-client/.../WebbitRpcContext.java](https://github.com/lostdj/GarageRpc/blob/master/src/webbit-client/main/java/name/ltp/garagerpc/server/webbit/WebbitRpcContext.java):
```java
public class WebbitRpcContext
{
	;
}
```

## License
Licensed under the CC0 1.0: http://creativecommons.org/publicdomain/zero/1.0/


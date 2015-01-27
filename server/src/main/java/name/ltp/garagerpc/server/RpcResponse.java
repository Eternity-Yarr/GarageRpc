package name.ltp.garagerpc.server;

class RpcResponse
{
	public Object v;
	public String error;

	RpcResponse(Object v, String error)
	{
		this.v = v;
		this.error = error;
	}
}

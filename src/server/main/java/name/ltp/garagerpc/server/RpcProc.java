package name.ltp.garagerpc.server;

import java.lang.reflect.Method;

class RpcProc
{
	public final Method m;
	public final Class args; // synthetic
	public final String[] orderedargs;

	RpcProc(Method m, Class args, String[] orderedargs)
	{
		this.m = m;
		this.args = args;
		this.orderedargs = orderedargs;
	}
}

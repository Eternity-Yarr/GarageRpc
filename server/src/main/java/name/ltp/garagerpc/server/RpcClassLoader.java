package name.ltp.garagerpc.server;

public class RpcClassLoader extends ClassLoader
{
	public Class<?> defineClass(String name, byte[] b)
	{
		return defineClass(name, b, 0, b.length);
	}
}

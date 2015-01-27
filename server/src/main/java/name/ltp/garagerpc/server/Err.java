package name.ltp.garagerpc.server;

public class Err
{
	//
	public static Err i() {if(instance == null) {instance = new Err();} return instance;}

	protected static Err i(Err i) {return instance == null ? instance = i : instance;}

	protected static Err instance;

	//
	public void err(String err)
	{
		System.err.println(err);
	}

	public void err(Throwable e)
	{
		System.err.println(e.toString());
	}
}


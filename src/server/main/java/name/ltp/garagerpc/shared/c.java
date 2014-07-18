package name.ltp.garagerpc.shared;

public class c<R, S>
{
	public R v;
	public String error;

	public final S serverContext;

	public c(S serverContext)
	{
		this.serverContext = serverContext;
	}
}

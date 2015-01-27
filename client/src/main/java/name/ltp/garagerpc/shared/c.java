package name.ltp.garagerpc.shared;

// Zee context.
public class c<R, S>
{
	public R v;
	public String error;

	public boolean error() {return error != null;}

	// Success!
	public void s(){}
	// Failure. :(
	public void f(){}
}

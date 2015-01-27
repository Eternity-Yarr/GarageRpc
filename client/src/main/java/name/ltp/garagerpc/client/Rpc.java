package name.ltp.garagerpc.client;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.plugins.ajax.Ajax;
import com.google.gwt.user.client.Window;
import name.ltp.garagerpc.shared.c;

public class Rpc
{
	public static void ajax(String rpcpath, String args, final c<?, ?> co, RetValCreator retvalcodec)
	{
		Ajax.ajax
			(
				Ajax.createSettings()
					.setUrl(rpcpath)
					.setDataType("json")
					.setType("post")
					//.setData(Properties.create(args))
					.setDataString(args)
					.setTimeout(42000)
					.setSuccess(new Succ(co, retvalcodec))
					.setError(new Fail(co, retvalcodec))
			);
	}

	//
	public static ErrPrinter errprinter = new ErrPrinter();

	public static class ErrPrinter
	{
		//
		public static native void console(String text)
		/*-{
			fancyweb20app.mainWindow.console.log(text);
		}-*/;

		//
		public void print(String err)
		{
			console(err);

			Window.alert(err);
		}
	}

	//
	private static class Succ extends RpcResponseFunc
	{
		public Succ(c<?, ?> co, RetValCreator retvalcodec)
		{
			super(co, retvalcodec);
		}

		@Override
		public void f()
		{
			decode();

			//Window.alert(getDataProperties().toJsonString());

			if(co != null && co.error != null)
				errprinter.print("RPC: Server replied with error: " + co.error);

			if(co != null)
				co.s();
		}
	}

	//
	private static class Fail extends RpcResponseFunc
	{
		public Fail(c<?, ?> co, RetValCreator retvalcodec)
		{
			super(co, retvalcodec);
		}

		@Override
		public void f()
		{
			decode();

			if(co != null && co.error != null)
				errprinter.print("RPC: Server replied with error: " + co.error);
			else if(co != null)
				errprinter.print(co.error = "RPC: Error: no reply from server");
			else
				errprinter.print("RPC: Error: no reply from server");

			if(co != null)
				co.f();
		}
	}

	//
	private static class RpcResponseFunc extends Function
	{
		public final c<?, ?> co;
		public final RetValCreator retvalcodec;

		private RpcResponseFunc(c<?, ?> co, RetValCreator retvalcodec)
		{
			this.co = co;
			this.retvalcodec = retvalcodec;
		}

		public void decode()
		{
			// Ugh.
			try
			{
				Object v = retvalcodec.jsoned.decode(new JSONObject(getDataProperties()));
				retvalcodec.decode(v, co);
			}
			catch(Exception ignored){}
		}
	}
}

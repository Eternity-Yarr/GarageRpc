package name.ltp.garagerpc.example.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import name.ltp.garagerpc.example.shared.RExampleService;
import name.ltp.garagerpc.server.webbit.WebbitRpcContext;
import name.ltp.garagerpc.shared.c;

import static com.google.gwt.query.client.GQuery.$;

public class Main implements EntryPoint
{
	static final RExampleService rpc = GWT.create(RExampleService.class);

	static final GQuery body = $("body");
	static final GQuery theinput = $("<input id=\"theinput\" type=\"text\" onkeydown=\"if(event.keyCode == 13) document.getElementById('thebutton').click()\">");
	static final GQuery thebutton = $("<input id=\"thebutton\" type=\"button\" value=\"Submit\">");

	@Override
	public void onModuleLoad()
	{
		final Function f =
			new Function()
			{
				@Override
				public void f()
				{
					submit();
				}
			};

		thebutton.click(f);

		body.append(theinput);
		body.append(thebutton);
	}

	void submit()
	{
		rpc.greet(
			$("#theinput").val(),
			new c<String, WebbitRpcContext>()
			{
				@Override
				public void s()
				{
					if(error == null)
						body.text(v);
				}

				@Override
				public void f()
				{
					body.text("Error");
				}
			});
	}
}

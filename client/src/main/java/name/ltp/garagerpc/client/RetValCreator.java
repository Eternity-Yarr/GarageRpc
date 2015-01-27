package name.ltp.garagerpc.client;

import name.ltp.garagerpc.shared.c;
import org.fusesource.restygwt.client.JsonEncoderDecoder;

public abstract class RetValCreator
{
	public final JsonEncoderDecoder jsoned;

	public RetValCreator(JsonEncoderDecoder jsoned)
	{
		this.jsoned = jsoned;
	}

	public abstract void decode(Object v, c co);
}

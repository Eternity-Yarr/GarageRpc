package name.ltp.garagerpc.server;

import com.google.gson.*;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.Paranamer;
import com.tonicsystems.jarjar.asm.Opcodes;
import name.ltp.garagerpc.shared.RpcPath;
import name.ltp.garagerpc.shared.RpcService;
import name.ltp.garagerpc.shared.c;
import org.mockito.cglib.core.TypeUtils;
import org.objectweb.asm.ClassWriter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class RpcHandler
{
	public static final Gson gson =
		new GsonBuilder()
			.serializeNulls()
			.registerTypeAdapter(
				Date.class,
				new JsonSerializer<Date>()
				{
					@Override
					public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context)
					{
						return src == null ? null : new JsonPrimitive(src.getTime());
					}
				}
			)
			.registerTypeAdapter(
				Date.class,
				new JsonDeserializer<Date>()
				{
					@Override
					public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
					{
						return json == null ? null : new Date(json.getAsLong());
					}
				}
			)
			.create();

	public static final RpcClassLoader classloader = new RpcClassLoader();

	public final String rpcpath;

	public final RpcService rpcservice;
	public final Map<String, RpcProc> procs = new HashMap<>();

	public RpcHandler(Class<? extends RpcService> rpcservice) throws Exception
	{
		this.rpcservice = rpcservice.newInstance();

		//
		{
			String rpcp = null;
			for(Class<?> sharediface : this.rpcservice.getClass().getInterfaces()) // Server class implements shared rpc iface.
				for(Class<?> rpcserviceiface : sharediface.getInterfaces())
					if(rpcserviceiface == RpcService.class) // Shared rpc iface extends dummy RpcService.
						for(Annotation a : sharediface.getAnnotations())
							if(a.annotationType() == RpcPath.class)
								rpcp = ((RpcPath)a).value();
			rpcpath = rpcp;
			if(rpcpath == null)
				throw new Exception("GarageRpc: Can't find " + RpcPath.class.getSimpleName() + " annotation for " + this.rpcservice.getClass().getName());
		}

		// Find implemented iface derived from RpcService iface.
		Class<?> iface = null;
		for(Class<?> derivedif : this.rpcservice.getClass().getInterfaces())
			if(iface == null)
				for(Class<?> rpcsif : derivedif.getInterfaces())
					if(rpcsif.getClass() == RpcService.class.getClass())
						iface = derivedif;

		//
		final String t = "" + System.currentTimeMillis();
		for(Method m : iface.getDeclaredMethods())
			if(m.getParameterTypes()[m.getParameterTypes().length - 1] != c.class)
				Err.i().err(
					this.rpcservice.getClass().getCanonicalName() + "." + m.getName()
						+ "(): type of last argument (" + m.getParameterTypes()[m.getParameterTypes().length - 1].getName()
						+ ") != " + c.class.getCanonicalName());
			else if(!((ParameterizedType)m.getGenericParameterTypes()[m.getParameterTypes().length - 1]).getActualTypeArguments()[0].toString().equalsIgnoreCase(m.getGenericReturnType().toString()))
				Err.i().err(
					this.rpcservice.getClass().getCanonicalName() + "." + m.getName()
						+ "(): return type mismatch: "
						+ m.getGenericReturnType().toString()
						+ " != " + ((ParameterizedType)m.getGenericParameterTypes()[m.getParameterTypes().length - 1]).getActualTypeArguments()[0].toString());
			else
				gen(this.rpcservice.getClass().getDeclaredMethod(m.getName(), m.getParameterTypes()), t);
	}

	void gen(Method m, String t)
	{
		//
		class arg
		{
			final String type;
			final Class ctype;

			final String ptype;
			final ParameterizedType cptype;

			final String name;

			final boolean iscontext;

			arg(String type, Class ctype, String ptype, ParameterizedType cptype, String name, boolean iscontext)
			{
				this.type = type;
				this.ctype = ctype;
				this.ptype = ptype;
				this.cptype = cptype;
				this.name = name;
				this.iscontext = iscontext;
			}

			boolean p()
			{
				return ptype != null;
			}
		}

		arg[] args = new arg[m.getParameterTypes().length];
		{
			int i = 0;
			final Paranamer paran = new BytecodeReadingParanamer();
			for(Type p : m.getGenericParameterTypes())
			{
				args[i] = new arg(
					m.getParameterTypes()[i].getCanonicalName(),
					m.getParameterTypes()[i],
					p instanceof ParameterizedType ? p.toString().replace("class ", "").replace("interface ", "").replace("$", ".") : null,
					p instanceof ParameterizedType ? (ParameterizedType)p : null,
					paran.lookupParameterNames(m, false)[i],
					i == args.length - 1);

				++i;
			}
		}

		// Method hash.
		final String hash;
		{
			StringBuilder sb = new StringBuilder();
			sb.append(m.getName());
			for(arg a : args)
				sb.append(a.p() ? a.ptype : a.type);

			hash = "" + (sb.toString().hashCode() & 0xFFFFFFFFL);
		}

		// Args class gen.
		final String procpkg = rpcservice.getClass().getPackage().getName() + "._" + t;
		final String procname = rpcservice.getClass().getSimpleName() + "_SrvProxy_" + hash;
		final String procfullname = (procpkg + "." + procname);
		ClassWriter cw = new ClassWriter(0);
		cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, procfullname.replace('.', '/'), null, "java/lang/Object", null);

		// First member field.
		cw.visitField(Opcodes.ACC_PUBLIC, "__rpcid",
			org.objectweb.asm.Type.getDescriptor(String.class), null, hash);

		// Other member fields and final class constructing.
		try
		{
			//
			for(int i = 0; i < args.length - 1; i++)
			{
				if(args[i].p())
				{
					// Don't ever repeat this anywhere else.
					// I was tired of looking for a better solution and found none.
					String binarytype = TypeUtils.parseType(args[i].ptype).getDescriptor();
					binarytype = binarytype.replaceAll("<", "<L");
					binarytype = binarytype.replaceAll(", ", ";L");
					binarytype = binarytype.replaceAll(">", ";>");

					cw.visitField(Opcodes.ACC_PUBLIC, args[i].name,
						org.objectweb.asm.Type.getDescriptor(args[i].ctype), binarytype, null);
				}
				else
					cw.visitField(Opcodes.ACC_PUBLIC, args[i].name,
						org.objectweb.asm.Type.getDescriptor(args[i].ctype), null, null);
			}

			//
			cw.visitEnd();
			byte[] bytes = cw.toByteArray();
			Class<?> clz = classloader.defineClass(procfullname, bytes);

			// Sigh. From Class.get[Declared]Fields():
			// 	'The elements in the array returned are not sorted and are not in any particular order'.
			// But we need to know the order to invoke our proc!
			String[] orderedargs = new String[args.length - 1];
			for(int i = 0; i < orderedargs.length; i++)
				orderedargs[i] = args[i].name;

			//
			procs.put(
				hash,
				new RpcProc(m, clz, orderedargs));
		}
		catch(Exception e)
		{
			Err.i().err(e);
		}
	}

	public String handle(c co, String __rpcid, String body)
	{
		RpcProc m = null;
		try
		{
			//
//			String __rpcid = request.body().substring(12);
			__rpcid = __rpcid.substring(0, __rpcid.indexOf("\""));

			//
			m = procs.get(__rpcid);
			if(m == null)
				throw new Exception("Remote procedure " + __rpcid + " not found");

			//
			final Object requestargs = gson.fromJson(body, m.args);
			final Object[] procargs = new Object[m.orderedargs.length + 1];
			for(int i = 0; i < m.orderedargs.length; i++)
				procargs[i] = requestargs.getClass().getField(m.orderedargs[i]).get(requestargs);
			procargs[procargs.length - 1] = co;

			//
			// TODO: Data structures/protocol versioning.
			co.v = m.m.invoke(rpcservice, procargs);
		}
		catch(Exception e)
		{
			co.v = null;

			if(co.error != null)
				co.error = co.error + "; " + e;
			else
				co.error = "" + e;

			Err.i().err(e);
		}

		//
		return gson.toJson(new RpcResponse(co.v, co.error));

//		if(r.length() < 64 * 1024)
//			Log.dbg("-> (" + rh.rpcservice.getClass().getSimpleName() + "." + (m != null ? m.m.getName() : "null") + "): " + r);
	}
}

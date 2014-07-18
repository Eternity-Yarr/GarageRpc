package name.ltp.garagerpc.rebind;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.*;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import name.ltp.garagerpc.shared.RpcPath;
import name.ltp.garagerpc.shared.c;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;

public class RpcGenerator extends Generator
{
	TreeLogger logger;
	GeneratorContext context;
	TypeOracle typeo;

	JClassType rpcs;

	String pkg;
	String oldname;
	String newname;

	SourceWriter srcw;

	String rpcpath;

	@Override
	public String generate(TreeLogger logger, GeneratorContext context, String typename) throws UnableToCompleteException
	{
		//
		this.logger = logger;
		this.context = context;
		typeo = context.getTypeOracle();

		rpcs = typeo.findType(typename);
		if(rpcs == null)
		{
			err("Can't find type " + typename);
			throw new UnableToCompleteException();
		}

		pkg = rpcs.getPackage().getName();
		oldname = rpcs.getSimpleSourceName();
		newname = oldname + "ClntImp";

		return mygen();
	}

	String mygen() throws UnableToCompleteException
	{
		//
		ClassSourceFileComposerFactory composer =
		 new ClassSourceFileComposerFactory(pkg, newname);

		composer.addImport("com.google.gwt.core.client.*");
		composer.addImport("com.google.gwt.user.client.*");
		composer.addImport("com.google.gwt.query.client.*");
		composer.addImport("static com.google.gwt.query.client.GQuery.$");
		composer.addImport("name.ltp.garagerpc.client.Rpc");
		composer.addImport(c.class.getCanonicalName());

		composer.addImplementedInterface(pkg + "." + oldname);

		//
		PrintWriter printWriter = context.tryCreate(logger, pkg, newname);
		if(printWriter == null)
			return pkg + "." + newname;

		srcw = composer.createSourceWriter(context, printWriter);
		if(srcw == null)
			return pkg + "." + newname;

		//
		for(Annotation a : rpcs.getAnnotations())
		 if(a.annotationType() == RpcPath.class)
			 rpcpath = ((RpcPath)a).value();
		if(rpcpath == null)
			err(RpcPath.class.getSimpleName() + " annotation not found on " + rpcs.getQualifiedSourceName());

		//
		UnableToCompleteException e = null;
		for(JMethod m : rpcs.getInheritableMethods())
		{
			JParameter[] ps = m.getParameters();
			JType last = ps[ps.length - 1].getType();
			JType rett = m.getReturnType();

			if(!last.getQualifiedSourceName().equalsIgnoreCase(c.class.getName()))
			{
				err(rpcs.getName() + "." + m.getName()
					+ "(): type of last argument ("
					+ last.getQualifiedSourceName()
					+ ") != " + c.class.getCanonicalName());

				e = new UnableToCompleteException();
			}
			else if(!last.isParameterized().getTypeArgs()[0].getParameterizedQualifiedSourceName().equalsIgnoreCase(rett.getParameterizedQualifiedSourceName()))
			{
					err(rpcs.getName() + "." + m.getName()
					+ "(): return type mismatch: "
					+ rett.getParameterizedQualifiedSourceName()
					+ " != " + last.isParameterized().getTypeArgs()[0].getParameterizedQualifiedSourceName());

				e = new UnableToCompleteException();
			}
			else
				genmeth(m);
		}
		if(e != null)
			throw e;

		//
		srcw.commit(logger);
		return pkg + "." + newname;
	}

	void genmeth(JMethod m)
	{
		p("");
		p("");
		p("");
		p("//----------------------------------------");
		p("");

		JParameter[] ps = m.getParameters();

		// Method hash.
		final String hash;
		{
			StringBuilder sb = new StringBuilder();
			sb.append(m.getName());
			for(JParameter p : ps)
				sb.append(p.getType().getParameterizedQualifiedSourceName());

			hash = "" + (sb.toString().hashCode() & 0xFFFFFFFFL);
		}

		// Class to serialize.
		final String jsoned = m.getName() + "_jsoned" + hash;
		p("public static class " + jsoned);
		p("{");
		{
			p("public String __rpcid = \"" + hash + "\";");

			for(JParameter p : ps)
			{
				if(p.getType().getQualifiedSourceName().equalsIgnoreCase(c.class.getCanonicalName()))
					continue;

				p("public " + p.getType().getParameterizedQualifiedSourceName() + " " + p.getName() + ";");
			}
		}
		p("}");

		// Boilerplate.
		{
			// Args.
			p("public interface I" + jsoned + " ");
			p("extends org.fusesource.restygwt.client.JsonEncoderDecoder<" + jsoned + ">{}");
			p("public static final I" + jsoned + " I" + jsoned + " = GWT.create(" + "I" + jsoned + ".class);");

			// Ret val.
			{
				//
				// TODO: Move data to a generic base class.
				p("public static class " + jsoned + "_retvalwrap ");
				p("{");
				{
					if(!m.getReturnType().getParameterizedQualifiedSourceName().equalsIgnoreCase(Void.class.getName()))
						p("public " + m.getReturnType().getParameterizedQualifiedSourceName()
							+ " v;");
					else
						p("public Object v;");

					p("public String error;");
				}
				p("}");

				//
				p("public interface I" + jsoned + "_retvalcodec ");
				p("extends org.fusesource.restygwt.client.JsonEncoderDecoder<"
					+ jsoned + "_retvalwrap" + ">{}");
				p("public static final I" + jsoned + "_retvalcodec"
					+ " I" + jsoned + "_retvalcodec" + " = GWT.create(" + "I" + jsoned + "_retvalcodec" + ".class);");

				//
				// TODO: Move to a generic base class.
				p("public static class RetValCreator" + jsoned +
					" extends name.ltp.garagerpc.client.RetValCreator");
				p("{");
				{
					p("public RetValCreator" + jsoned + "(org.fusesource.restygwt.client.JsonEncoderDecoder jsoned)");
					p("{"); p("super(jsoned);"); p("}");

					p("@Override public void decode(Object v, c co)");
					p("{");
					{
						p(jsoned + "_retvalwrap vv = (" + jsoned + "_retvalwrap)v;");
//						if(!m.getReturnType().getParameterizedQualifiedSourceName().equalsIgnoreCase(Void.class.getName()))
//							p("co.v = (" + m.getReturnType().getParameterizedQualifiedSourceName() + ")vv.v;");
//						else
//							p("co.v = vv.v;");
						p("if(co != null)");
						p("{");
							p("co.v = vv.v;");
							p("co.error = vv.error;");
						p("}");
					}
					p("}");

					p("public static final RetValCreator" + jsoned + " __instance = new RetValCreator" + jsoned
						+ "(" + "I" + jsoned + "_retvalcodec" + ");");
				}
				p("}");
			}
		}

		// Method itself.
		p("@Override public " + m.getReturnType().getQualifiedSourceName() + " " + m.getName());
		//  Args.
		p("(");
		{
			int i = ps.length;
			for(JParameter p : ps)
			{
				p(p.getType().getParameterizedQualifiedSourceName() + " " + p.getName());


				if(--i > 0)
					p(", ");
			}
		}
		p(")");
		// Actual method body.
		p("{");
		{
			//
			p("final " + jsoned + " args = new " + jsoned + "();");
			for(JParameter p : ps)
			{
				if(p.getType().getQualifiedSourceName().equalsIgnoreCase(c.class.getCanonicalName()))
					continue;

				p("args." + p.getName() + " = " + p.getName() + ";");
			}

			//
			p("Rpc.ajax("
			 + "\"" + rpcpath + "\"" + ", "
			 + "I" + jsoned + ".encode(args).toString()" + ", "
			 + ps[ps.length - 1].getName() + ", "
			 + "RetValCreator" + jsoned + ".__instance"
			 + ");");

			if(m.getReturnType().getSimpleSourceName().equals("void"))
				p("return;");
			else if(m.getReturnType().isPrimitive() != null)
				p("return 0;");
			else
				p("return null;");
		}
		p("}");
	}

	protected SourceWriter p()
	{
		srcw.println();

		return srcw;
	}

	public SourceWriter p(String value)
	{
		//inf(value + "\r\n");

		srcw.println(value);

		return srcw;
	}

	void inf(String s)
	{
		logger.log(TreeLogger.Type.INFO, s);
	}

	void err(String s)
	{
		logger.log(TreeLogger.Type.ERROR, s);
	}
}

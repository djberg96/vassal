/*
 ****************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one                *
 * or more contributor license agreements.  See the NOTICE file              *
 * distributed with this work for additional information                     *
 * regarding copyright ownership.  The ASF licenses this file                *
 * to you under the Apache License, Version 2.0 (the                         *
 * "License"); you may not use this file except in compliance                *
 * with the License.  You may obtain a copy of the License at                *
 *                                                                           *
 *     http://www.apache.org/licenses/LICENSE-2.0                            *
 *                                                                           *
 * Unless required by applicable law or agreed to in writing,                *
 * software distributed under the License is distributed on an               *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY                    *
 * KIND, either express or implied.  See the License for the                 *
 * specific language governing permissions and limitations                   *
 * under the License.                                                        *
 *                                                                           *
 * This file is part of the BeanShell Java Scripting distribution.           *
 * Documentation and updates may be found at http://www.beanshell.org/       *
 * Patrick Niemeyer (pat@pat.net)                                            *
 * Author of Learning Java, O'Reilly & Associates                            *
 *                                                                           *
 *****************************************************************************/


package bsh;

import java.lang.reflect.InvocationTargetException;

class BSHMethodInvocation extends SimpleNode
{
	private static final long serialVersionUID = 0L;

	BSHMethodInvocation (int id) { super(id); }

	BSHAmbiguousName getNameNode() {
		return (BSHAmbiguousName)jjtGetChild(0);
	}

	BSHArguments getArgsNode() {
		return (BSHArguments)jjtGetChild(1);
	}

	/**
		Evaluate the method invocation with the specified callstack and 
		interpreter
	*/
	public Object eval( CallStack callstack, Interpreter interpreter )
		throws EvalError
	{
		NameSpace namespace = callstack.top();
		BSHAmbiguousName nameNode = getNameNode();

		// Do not evaluate methods this() or super() in class instance space
		// (i.e. inside a constructor)
		if ( namespace.getParent() != null && namespace.getParent().isClass
			&& ( nameNode.text.equals("super") || nameNode.text.equals("this") )
		)
			return Primitive.VOID;
 
		Name name = nameNode.getName(namespace);
		Object[] args = getArgsNode().getArguments(callstack, interpreter);

		return invokeWithErrorHandling(
			"Method Invocation " + name, this, callstack,
			() -> name.invokeMethod( interpreter, args, callstack, this)
		);
	}

	static Object invokeWithErrorHandling(
		String message, SimpleNode node, CallStack callstack, Invoker invoker)
		throws EvalError
	{
		try {
			return invoker.invoke();
		} catch ( ReflectError e ) {
			throw new EvalError(
				"Error in method invocation: " + e.getMessage(), 
				node, callstack, e );
		} catch ( InvocationTargetException e )
		{
			Throwable te = e.getTargetException();

			/*
				Try to squelch the native code stack trace if the exception
				was caused by a reflective call back into the bsh interpreter
				(e.g. eval() or source()
			*/
			boolean isNative = true;
			if ( te instanceof EvalError )
				if ( te instanceof TargetError )
					isNative = ((TargetError)te).inNativeCode();
				else
					isNative = false;

			throw new TargetError( message, te, node, callstack, isNative );
		} catch ( UtilEvalError e ) {
			throw e.toEvalError( node, callstack );
		}
	}

	interface Invoker {
		Object invoke()
			throws EvalError, InvocationTargetException, ReflectError, UtilEvalError;
	}
}

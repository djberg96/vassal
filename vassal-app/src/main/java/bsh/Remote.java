/*
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
 *                                                                           *
 * This file is part of the BeanShell Java Scripting distribution.           *
 * Documentation and updates may be found at http://www.beanshell.org/       *
 * Patrick Niemeyer (pat@pat.net)                                            *
 * Author of Learning Java, O'Reilly & Associates                            *
 *                                                                           *
 *****************************************************************************/


package bsh;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
/**
	Remote executor class. Posts a script from the command line to a BshServlet
 	or embedded  interpreter using (respectively) HTTP or the bsh telnet
	service. Output is printed to stdout and a numeric return value is scraped
	from the result.
*/
public class Remote
{
	private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(60);
	private static final HttpClient HTTP = HttpClient.newBuilder()
		.connectTimeout(HTTP_TIMEOUT)
		.followRedirects(HttpClient.Redirect.NORMAL)
		.build();

    public static void main( String args[] )
		throws Exception
	{
		if ( args.length < 2 ) {
			System.out.println(
				"usage: Remote URL(http|bsh) file [ file ] ... ");
			System.exit(1);
		}
		String url = args[0];
		String text = getFile(args[1]);
		int ret = eval( url, text );
		System.exit( ret );
		}

	/**
		Evaluate text in the interpreter at url, returning a possible integer
	 	return value.
	*/
	public static int eval( String url, String text )
		throws IOException
	{
		String returnValue = null;
		if ( url.startsWith( "http:" ) ) {
			returnValue = doHttp( url, text );
		} else if ( url.startsWith( "bsh:" ) ) {
			returnValue = doBsh( url, text );
		} else
			throw new IOException( "Unrecognized URL type."
				+"Scheme must be http:// or bsh://");

		try {
			return Integer.parseInt( returnValue );
		} catch ( Exception e ) {
			// this convention may change...
			return 0;
		}
	}

	static String doBsh( String url, String text ) 
	{ 
	    String host = "";
	    String port = "";
	    String returnValue = "-1";
		String orgURL = url;
	    
		// Need some format checking here
	    try {
			url = url.substring(6); // remove the bsh://
			// get the index of the : between the host and the port is located
			int index = url.indexOf(":");
			host = url.substring(0,index);
			port = url.substring(index+1,url.length());
		} catch ( Exception ex ) {
			System.err.println("Bad URL: "+orgURL+": "+ex  );
			return returnValue;
	    }

	    try (Socket s = new Socket(host, Integer.parseInt(port) + 1);
	         BufferedReader bin = new BufferedReader(
		         new InputStreamReader(s.getInputStream()))) {
			System.out.println("Connecting to host : " 
				+ host + " at port : " + port);
			
			final OutputStream out = s.getOutputStream();
			sendLine( text, out );

			  String line;
			  while ( (line=bin.readLine()) != null )
				System.out.println( line );

			// Need to scrape a value from the last line?
			returnValue="1";
			return returnValue;
	    } catch(Exception ex) {
			System.err.println("Error communicating with server: "+ex);
			return returnValue;
	    }
	}

    private static void sendLine( String line, OutputStream outPipe )
		throws IOException
	{
		outPipe.write( line.getBytes( StandardCharsets.UTF_8 ) );
		outPipe.flush();
    }


	static String doHttp( String postURL, String text )
	{
		String returnValue = null;
		String formData = buildFormData( text );

		try {
		  HttpRequest request = HttpRequest.newBuilder( URI.create( postURL ) )
			  .timeout( HTTP_TIMEOUT )
			  .POST( HttpRequest.BodyPublishers.ofString( formData, StandardCharsets.UTF_8 ) )
			  .header( "Content-Type", "application/x-www-form-urlencoded; charset=UTF-8" )
			  .build();
		  HttpResponse<String> response = HTTP.send(
			  request,
			  HttpResponse.BodyHandlers.ofString( StandardCharsets.UTF_8 )
		  );

		  int rc = response.statusCode();
		  if ( rc != 200 )
			System.out.println("Error, HTTP response: "+rc );

		  returnValue = response.headers().firstValue("Bsh-Return").orElse(null);

		  response.body().lines().forEach( System.out::println );

		  System.out.println( "Return Value: "+returnValue );

		} catch (IllegalArgumentException e) {
		  System.out.println(e);     // bad postURL
		} catch (IOException e2) {
		  System.out.println(e2);    // I/O error
		} catch (InterruptedException e3) {
		  Thread.currentThread().interrupt();
		  System.out.println(e3);
		}

		return returnValue;
	}

	static String buildFormData( String text )
	{
		return "bsh.client=Remote&bsh.script="
			+ URLEncoder.encode( text, StandardCharsets.UTF_8 );
	}

	static String getFile( String name )
		throws FileNotFoundException, IOException
	{
		return Files.readString( Path.of( name ), StandardCharsets.UTF_8 );
	}

}

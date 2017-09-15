package ufrn.dimap.lets.ehmetrics.repositoryexplorer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GitHubConnector
{
	private static final String baseURL = "https://api.github.com/";
	private static final String charset = StandardCharsets.UTF_8.name();
	
	public static String getJsonResponse ( String suffixURL, String query ) throws MalformedURLException, IOException
	{
		HttpURLConnection connection = (HttpURLConnection) new URL(baseURL + suffixURL + "?" + query).openConnection();
		connection.setRequestProperty("Accept-Charset", charset);
		
		
		int status = connection.getResponseCode();
		System.out.println("Status: " + status);
		
		System.out.println("Headers: ");
		System.out.println("Rate limit remaining: " + connection.getHeaderField("X-RateLimit-Remaining"));
		System.out.println("Rate limit reset as: " + connection.getHeaderField("X-RateLimit-Reset"));
		
		System.out.println("Link: " + connection.getHeaderField("Link"));
		/*
		for (Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
		    System.out.println(header.getKey() + "=" + header.getValue());
		}
		*/
		/*
		for (String param : contentType.replace(" ", "").split(";")) {
		    if (param.startsWith("charset=")) {
		        charset = param.split("=", 2)[1];
		        break;
		    }
		}
		*/
		
		InputStream response = connection.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(response));
		return reader.readLine();
	}
}

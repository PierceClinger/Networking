import java.io.*;
import java.net.*;
import java.util.*;

public class HttpClient {

    public static void main(String[] args) throws IOException {
        URL url = new URL(args[0]);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	connection.setFollowRedirects(true);
        connection.setRequestMethod("GET");

	BufferedWriter writer = new BufferedWriter(new FileWriter("./HttpClientOutput", true));

	Map<String, List<String>> map = connection.getHeaderFields();
	    for (Map.Entry<String, List<String>> entry : map.entrySet()) {
	        System.out.println("Key : " + entry.getKey() + " ,Value : " + entry.getValue());
		writer.write("Key : " + entry.getKey() + " ,Value : " + entry.getValue() + "\n");
	    }

	InputStream output = connection.getInputStream();
	Scanner scanner = new Scanner(output).useDelimiter("\\A");
	String result = scanner.hasNext() ? scanner.next() : "";
	writer.write(result);

	writer.close();
        InputStream responseStream = connection.getInputStream();
        System.out.println(responseStream);
    }
}

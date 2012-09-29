package shallowgreen.visualizer.servlet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import shallowgreen.visualizer.Visualizer;

public class OverlayServlet extends HttpServlet {
	private static final long serialVersionUID=9007125313581637293L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		doGet(request,response);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		// FIXME: most definitely far from efficient
		BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
		String page=readFile("overlay.html");
                if (page == null)  {
                    System.err.println("HALP! overlay.html is null :(");
		}
		if(Visualizer.gameURL!=null)
			bw.write(page.replace("___IFRAMEURL___",Visualizer.gameURL));
		else
			bw.write(page.replace("___IFRAMEURL___",""));
		bw.flush();
		bw.close();
	}

	private static String readFile(String path) throws IOException {
		FileInputStream stream=new FileInputStream(new File(path));
		try {
			FileChannel channel=stream.getChannel();
			MappedByteBuffer bb=channel.map(FileChannel.MapMode.READ_ONLY,0,channel.size());
			return Charset.defaultCharset().decode(bb).toString();
		} finally {
			stream.close();
		}
	}

}

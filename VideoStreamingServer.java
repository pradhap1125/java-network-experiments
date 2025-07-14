import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;

public class VideoStreamingServer {
    public static void main(String[] args) throws IOException {
        int port = 8000;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/video", new VideoHandler());
        server.setExecutor(null);
        server.start();
    }

    static class VideoHandler implements HttpHandler {
        private static final String VIDEO_FOLDER = "C:\\Users\\sample_videos";

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            if (query == null || !query.startsWith("name=")) {
                exchange.sendResponseHeaders(400, -1); // Bad request
                return;
            }

            String fileName = query.substring(5); // Get filename from query param
            File videoFile = new File(VIDEO_FOLDER, fileName);

            if (!videoFile.exists()) {
                exchange.sendResponseHeaders(404, -1); // Not found
                return;
            }

            Headers headers = exchange.getResponseHeaders();
            headers.add("Content-Type", "video/mp4");

            long fileLength = videoFile.length();
            String range = exchange.getRequestHeaders().getFirst("Range");
            //loads 8kB data on every buffer
            long start = 0, end = 8192;

            if (range != null && range.startsWith("bytes=")) {
                String[] parts = range.substring(6).split("-");
                try {
                    start = Long.parseLong(parts[0]);
                    if ((parts.length > 1) && !parts[1].isEmpty()){
                    	end=Long.parseLong(parts[1]);
                    }else {
                    	end=start+8192;
                    }
                } catch (NumberFormatException e) {
                    exchange.sendResponseHeaders(416, -1); // Range Not Satisfiable
                    return;
                }         
               
            }
            if (end >= fileLength) {
                end = fileLength - 1; // end is inclusive
            }
            headers.add("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
            exchange.sendResponseHeaders(206, end - start + 1); // Always return partial
            

            try (RandomAccessFile raf = new RandomAccessFile(videoFile, "r");
                 OutputStream os = exchange.getResponseBody()) {
                raf.seek(start);
                byte[] buffer = new byte[8192];
                long bytesLeft = end - start + 1;

                while (bytesLeft > 0) {
                    int bytesToRead = (int) Math.min(buffer.length, bytesLeft);
                    int bytesRead = raf.read(buffer, 0, bytesToRead);
                    if (bytesRead == -1) break;
                    os.write(buffer, 0, bytesRead);
                    bytesLeft -= bytesRead;
                }
            }
        }
    }
}

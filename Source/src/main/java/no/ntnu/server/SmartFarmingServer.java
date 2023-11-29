import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SmartFarmingServer {

    private static final int PORT = 12345;
    private static final int THREAD_POOL_SIZE = 10;

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Smart Farming Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection established.");

                // Handle each client connection using a thread from the pool
                executorService.submit(new ClientHandler(clientSocket));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
    }
}
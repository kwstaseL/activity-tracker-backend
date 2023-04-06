import java.net.Socket;


// This class will handle the client connection
public class ClientHandler implements Runnable
{
    // This is the socket that the client is connected to
    private Socket clientSocket = null;

    public ClientHandler(Socket clientSocket)
    {
        this.clientSocket = clientSocket;
    }

    // This is where the client will be handled
    public void run()
    {

    }



}

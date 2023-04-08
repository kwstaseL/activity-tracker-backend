import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Queue;


// This class will handle the client connection
public class ClientHandler implements Runnable
{
    // This is the socket that the client is connected to
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    private Queue<File> filesFromClient;

    public ClientHandler(Socket clientSocket, Queue<File> filesFromClient)
    {
        this.clientSocket = clientSocket;
        this.filesFromClient = filesFromClient;
    }


    // This is where the client will be handled
    public void run()
    {
        while (!clientSocket.isClosed())
        {
            try {

                in = new ObjectInputStream(clientSocket.getInputStream());
                out = new ObjectOutputStream(clientSocket.getOutputStream());

                // Receive the file object from the client
                Object receivedObject = in.readObject();

                if (receivedObject instanceof File receivedFile)
                {
                    System.out.println("Received file: " + receivedFile.getName());
                }

            } catch (IOException | ClassNotFoundException e)
            {
                System.out.println("Connection to client lost");
                e.printStackTrace();
            }
            finally
            {
                close();
            }
        }

    }

    // This method will close the connection to the client
    // and clean up the resources
    private void close()
    {
        try
        {
            if (in != null)
            {
                in.close();
            }
            if (out != null)
            {
                out.close();
            }
            if (clientSocket != null)
            {
                clientSocket.close();
            }

        }
        catch  (IOException e)
        {
            throw new RuntimeException(e);
        }

    }




}
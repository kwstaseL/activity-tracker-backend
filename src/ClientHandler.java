import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Queue;


// This class will handle the client connection
public class ClientHandler implements Runnable
{
    // This is the socket that the client is connected to
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    private Queue<File> filesFromClient;

    public static ArrayList<ClientHandler> clients = new ArrayList<>();

    public ClientHandler(Socket clientSocket, Queue<File> filesFromClient)
    {
        this.clientSocket = clientSocket;
        this.filesFromClient = filesFromClient;

        try
        {
            this.in = new ObjectInputStream(clientSocket.getInputStream());
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
        }
        catch (IOException e)
        {
            System.out.println("Could not create input and output streams");
            System.out.println("Error: " + e.getMessage());
        }
        clients.add(this);
    }


    // This is where the client will be handled
    public void run()
    {
        try
        {
            while (!clientSocket.isClosed())
            {
                // Receive the file object from the client
                Object receivedObject = in.readObject();

                if (receivedObject instanceof File receivedFile) {
                    System.out.println("Received file (ClientHandler): " + receivedFile.getName());
                    synchronized (filesFromClient)
                    {
                        filesFromClient.add(receivedFile);
                        filesFromClient.notify();
                    }
                }
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

            clients.remove(this);
            System.out.println("Client disconnected");
        }
        catch  (IOException e)
        {
            throw new RuntimeException(e);
        }

    }


}

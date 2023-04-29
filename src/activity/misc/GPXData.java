package activity.misc;

import java.io.Serializable;

public class GPXData implements Serializable
{
    private String fileName;
    private byte[] fileContent;

    public GPXData(String fileName, byte[] fileContent)
    {
        this.fileName = fileName;
        this.fileContent = fileContent;
    }

    public String getFileName()
    {
        return fileName;
    }

    public byte[] getFileContent()
    {
        return fileContent;
    }
}
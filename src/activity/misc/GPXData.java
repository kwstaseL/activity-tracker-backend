package activity.misc;

import java.io.Serializable;

public class GPXData implements Serializable
{
    private final String fileName;
    private final byte[] fileContent;
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

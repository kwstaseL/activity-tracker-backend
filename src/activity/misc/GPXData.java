package activity.misc;

import java.io.Serializable;

public class GPXData implements Serializable
{
    private final byte[] fileContent;
    public GPXData(byte[] fileContent)
    {
        this.fileContent = fileContent;
    }

    public byte[] getFileContent()
    {
        return fileContent;
    }
}

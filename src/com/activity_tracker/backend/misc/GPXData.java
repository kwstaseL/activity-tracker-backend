package com.activity_tracker.backend.misc;

import java.io.Serializable;

/**
 * This class is used to send the GPX file from the client to the master
 */
public class GPXData implements Serializable
{
    // fileContent: represents the content of the GPX file
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

package gwtupload.client.dnd;

import gwtupload.client.FileList;

/**
 * IDragAndDropFileInput.
 *
 * @author Sultan Tezadov
 */
public interface IDragAndDropFileInput {

    public boolean hasFiles();

    public FileList getFiles();

    public String getName();

    public void reset();
    
    public void lock();
}

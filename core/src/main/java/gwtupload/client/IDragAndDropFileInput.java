package gwtupload.client;

/**
 * IDragAndDropFileInput.
 *
 * @author Sultan Tezadov
 * @since Jan 20, 2014
 */
public interface IDragAndDropFileInput {

    public boolean thereAreDragAndDropedFiles();

    public FileList getDragAndDropedFiles();

    public String getName();
}

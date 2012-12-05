package gwtupload.client;

import java.util.ArrayList;
import java.util.List;

public class ServerInfo {

	private List<UploadedInfo> uploadedFiles;
	private String message;
	private String field;
	
	public ServerInfo() {
		reset();
	}
	
	public void reset() {
		this.uploadedFiles = new ArrayList<UploadedInfo>();
	}
	
	public List<UploadedInfo> getUploadedFiles() {
		return uploadedFiles;
	}
	
	public void add(UploadedInfo info) {
		uploadedFiles.add(info);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getField() {
		return field;
	}
	
	public int getTotalSize() {
		int size = 0;
		for (UploadedInfo info: uploadedFiles) {
			size += info.getSize();
		}
		return size;
	}
	
	public List<String> getUploadedFileNames() {
    List<String> result = new ArrayList<String>();
    for (UploadedInfo info: uploadedFiles) {
      result.add(info.getFileName());
    }
    return result;
  }
}

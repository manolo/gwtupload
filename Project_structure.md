# Introduction

GwtUpload uses iframes to permit an asynchronous form submit. A Form is generated for each independent file submit. A file id is also generated to permit automatic progress check without the intervention of the client programmer.


# Details

Add your content here.  Format your content with:
- Text in **bold** or *italic*
- Headings, paragraphs, and lists
- Automatic links to other wiki pages

I'd add a 'architecture' wiki page with the following content. Tell me please if it's already done:

## logical process

gwtupload.client.Uploader is the core object for the client server communication. It uses mainly two methods for that:
**submit()** to submit a file to the server 
and 
**update()** to refresh the upload progress status and widget

**submit()** does a regular submit using as the form target the id of a genereated element for the purpose. 
From that point, a timer gwtupload.client.Uploader#statusInterval triggers calls to the **update()** method that asynchroneously checks the server update status (ther server uses AbstractUploadListener to update the upload progress state independently of the client processes as defined by commons file upload in ProgressListener)

FormPanel and FormPanelImpl generate the form inside an iFrame. Knowing that FormPanel is handling the java generic operations while FormPanelImpl handles the native ones. 

Actually Uploader uses an instance of its inner class FormFlowPanel that extends FormPanel. The latter contains an instance of FormPanelImpl. 
FormFlowPanel permits to add widgets to the form.

The browser native <input type="file"> is hidden in IFileInput implementations. Uploader#fileInput. The defacto base implementation of it is FileUploadWithMouseEvents.


## graphical process

when submit() is called, a widget subclassing BaseUploadStatus is created through ... and inserted to ... to hold the lifecycle of the uploaded file. From this moment, the Uploader class that holds the instance of its graphical representation  IUploadStatus (default BaseUploadStatus) updates its state throught the setProgress() method. 
Internally Uploader has an installed receiver for each type of event #onStatusReceivedCallback (for status updates), #onDeleteFileCallback (to handle deletion), etc...  (

those callbacks could be declared protected in my opinion or at least accessed through a protected getter
parseAjaxResponse() could also be protected so it can be feature extended without the need to copy paste all the methods

## extention points

To override core functionality, you will have to override the Uploader class, and handle the events you need to handle by overriding submit(), update() or cancel()

To have you own representation of IUploadStatus you have to ...

To add checks before adding a file (eg. limit the file maximum size) you have to...

To add the Html5 thumbnail preview, or to show an icon representing the file you're uploading (word, excel, pdf,..) you can override ...
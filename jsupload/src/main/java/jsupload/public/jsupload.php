<?php
#
# Copyright 2010 Manolo Carrasco Moñino. (manolo at apache/org)
# Thanks to 'Guangyu' who wrote the first version of this script (see issue #91)
# http://code.google.com/p/gwtupload
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.
#

############################################################################################
## * * Server script written in php for the GWTUpload/JSUpload library.
## *
## * * This script requires PHP with PHP-APC.
## *
## * * You have to put this file in a php enabled folder of your web server.
## *
## * # *Server Configuration*:
## *   In order to enable the PHP APC extension to support file upload progress indication
## *   you have to add the following section into php.ini:
## *   {{{
## *    [APC]
## *    apc.enabled=1
## *    apc.shm_segments=1
## *    apc.shm_size=64
## *    apc.max_file_size=10M
## *    apc.stat=1
## *    apc.rfc1867="1"
## *    apc.rfc1867_freq="0"
## *   }}}
## * # *Client side*:
## *   In the client you have to add a hidden widget to the uploader in the first position.
## *   * Gwt-upload
## *     {{{
## *       uploader.add(new Hidden("APC_UPLOAD_PROGRESS", uploader.getInputName()), 0);
## *     }}}
## *   * Js-upload
## *     {{{
## *       var e = document.createElement("INPUT")
## *       e.type = "hidden";
## *       e.name = "APC_UPLOAD_PROGRESS";
## *       e.value = u.data().name;
## *       u.addElement(e, 0);
## *     }}}
## * # *Configuration*: At the top of the script there is a customizable variable.
## *   * $uploaddir: is the prefix used in the path location to store the data received. By default
## *                 it is set to "/tmp/php_upload/", normally you have to change it to match the path
## *                 configured in your application. It is strongly recommended that this directory 
## *                 should be created apart from "/tmp", in a non-httpd accessible path.
## * # *Integration*:
## *   * The files are stored in the folder $tmp_dir/xxxx where xxxx is the session id (cookie PHPSESSID).
## *   * For each file received, it stores a file /tmp/uploader/xxxx/yyyy.info, 
## *     where yyyy is the name of the element, with the original filename and its content-type.
## *   * The content of the file in the file will be in the /tmp/uploader/xxxx/yyyy.bin file.
## *   * The application must create, handle, and clean $tmp_dir files.
## *   * You should check that the user session is valid in order to avoid that your server could
## *     be used as a used as a surreptitious storage area.
############################################################################################

session_start();

$uploaddir = '/tmp/php_upload/' . session_id() . "/";
$version  = '0.6.4';

function writeResponse($msg, $post) {
  $xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" .
         "\n<response>\n" . $msg . "\n</response>\n";
  if ($post) {
    # IE has issues when reading an XML in an iframe. We wrap the xml
    # message so as the client is able to compose the original one.
    # client side will extract the text between %%%INI%%% and %%%END%%% tags
    # and will restore '<' and '>' symbols.
    $xml = str_replace("<", "@@@", $xml);
    $xml = str_replace(">", "___", $xml);
    $xml = '%%%INI%%%' + $xml + '%%%END%%%';
  }
  die($xml);
}

if($_SERVER['REQUEST_METHOD'] == 'GET') {
  # Get is used to request percent while the file is uploaded.
  # It is used also to remove the uploaded file, get it and cancel.
  if(isset($_GET['filename'])) {
    header('Expires: Tue, 08 Oct 1991 00:00:00 GMT');
    header('Cache-Control: no-cache, must-revalidate');

    if($_SESSION['canceled'] == true) {
        $_SESSION['canceled'] = false;
        writeResponse("<canceled>true</canceled><finished>canceled</finished>", 0);
    }

    $status = apc_fetch('upload_' . $_GET['filename']);
    if ($status['total']) {
      $done = (int)$status['current'];
      $total = (int)$status['total'];
      $percent = round($done/$total*100);

      $resp = sprintf("<percent>%d</percent><currentBytes>%d</currentBytes><totalBytes>%d</totalBytes>", 
              $percent, $done, $total);
      if($done==$total) $resp .= "<finished>ok</finished>";
        writeResponse($resp, 0);
      } else {
        writeResponse("<error>" . $_GET['filename'] . "</error>", 0);
      }
  } else if(isset($_GET['new_session'])) {
    if (!file_exists($uploaddir))
      mkdir($uploaddir);
    writeResponse("<session>ok</session>", 0);
  } else if(isset($_GET['show'])){
    $file = $uploaddir . $_GET['show'] . ".bin";
    $info = $uploaddir . $_GET['show'] . ".info";
    if (file_exists($info)) {  
      $lines = file($info);
      header('Content-Type: ' . $lines[1]);
    }
    if(file_exists($file)) {
      readfile($file);
    }
  } else if(isset($_GET['remove'])) {
    $file = $uploaddir . $_GET['remove'] . ".bin";
    $info = $uploaddir . $_GET['remove'] . ".info";
    if(file_exists($file))
      unlink($file);
    if(file_exists($info))
      unlink($info);
    writeResponse("<deleted>true</deleted>", 0);
  } else if(isset($_GET['cancel'])) {
    $_SESSION['canceled'] = true;
    writeResponse("<canceled>true</canceled>", 0); 
  } else {
    writeResponse("<error>no parameter</error>", 0); 
  }
} else { //POST
  # Post is used to receive the files and move them to the user's
  # session folder.
  $_SESSION['canceled'] = false;
  session_write_close();

  if(!$_POST['APC_UPLOAD_PROGRESS']) {
    writeResponse("<error>You have not sent the APC_UPLOAD_PROGRESS parameter.</error>", 1);
  }
  $key = $_POST['APC_UPLOAD_PROGRESS'];

  if(!$_FILES[$key]) {
    writeResponse("<error>You have sent an incorrect APC_UPLOAD_PROGRESS key:" . $key . "</error>", 1);
  }

  $uploadfile = $uploaddir . $key . ".bin";
  $uploadinfo = $uploaddir . $key . ".info";

  if (move_uploaded_file($_FILES[$key]['tmp_name'], $uploadfile)) {
    $size = $_FILES[$key]['size'];
    $type = $_FILES[$key]['type'];
    $name = $_FILES[$key]['name'];
    $servermessage = $version;

    $msg  = " <file>\n  <field>$key</field>\n  <name>$name</name>"
          . "\n  <size>$size</size>\n  <ctype>$type</ctype>"
          . "\n  <message>\n<![CDATA[\n$servermessage\n]]>\n  </message>\n </file>";

    $fh = fopen($uploadinfo, 'w');
    fwrite($fh, $name . "\n" . $type . "\n");    
    fclose($fh);

    writeResponse($msg, 1);
  } else {
    writeResponse("<error>Unable to move: " . $_FILES[$key]['tmp_name'] . " to: " . $uploadfile . "</error>", 1);
  }
}

?>

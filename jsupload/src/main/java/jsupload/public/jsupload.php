<?php
# To enable the PHP APC extension to support file upload progress indication, following section must be put into php.ini:
# [APC]                                                                                                                               
# apc.enabled=1                                                                                                                       
# apc.shm_segments=1                                                                                                                  
# apc.shm_size=64                                                                                                                     
# apc.max_file_size=10M                                                                                                               
# apc.stat=1                                                                                                                          
# apc.rfc1867="1"                                                                                                                     
# apc.rfc1867_freq="0" 

header('Expires: Tue, 08 Oct 1991 00:00:00 GMT');
header('Cache-Control: no-cache, must-revalidate');
 
if(isset($_GET['filename'])){
  $status = apc_fetch('upload_' . $_GET['filename']);
  if ($status['total']) {
    $done = $status['current'];
    $total = $status['total'];
   	$percent =  round($done/$total*100);
    echo "Content-Type: text/plain\n$set_cookie\n"
        . "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
        . "\n<response>\n"
        . "<percent>$percent</percent>"
        . "<currentBytes>$done</currentBytes>"
        . "<totalBytes>$total</totalBytes>"
        . "</response>\n";
  } else {
   	echo 0;
  }
}
?>

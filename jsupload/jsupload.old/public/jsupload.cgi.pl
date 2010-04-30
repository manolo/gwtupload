#!/usr/bin/perl -w
#
# Copyright 2009 Manolo Carrasco Moñino. (manuel_carrasco at users.sourceforge.net)
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
## * The provided server script has been written in perl for the GWTUpload/JSUpload library.
## *
## * This script requires perl, CGI and Digest::MD5, which are installed by default in
## * most linux/unix distributions.
## *
## * You have to put this file in a script/cgi-bin enabled folder of your web server, and
## * you have to set the rigth execution permissions.
## *
## * When it receives a POST request, it updates periodically a status file with the progress, 
## * and stores all received form elements in a temporary folder for the user session.
## *
## * When it receives GET requests, returns a xml response with the progess information.
## *
## * When the GET request, has the parameter show=xxxx, it returns the content of the form
## * element whose name is xxxx. If the element is a file it sends a response with the adequate
## * content-type, otherwise it returns a xml response with the element's value.
## *
## * The files are stored in the folder /tmp/uploader/xxxx where xxxx is the session id (cookie CGISESSID)
## * For each form element received, it stores a file /tmp/uploader/xxxx/yyyy.info where yyyy is 
## * the name of the element. In the case of file elements, it puts the content of the file in
## * /tmp/uploader/xxxx/yyyy.bin. 
## *
## * This script doesn't clean /tmp/uploader files. So the application responsible of handling
## * this files has to clean them.
############################################################################################

use CGI;
use Digest::MD5;
use File::Path;
use strict;
use warnings;
use Data::Dumper;

my $idname   = "CGISESSID";
my $tmp_dir  = "/tmp/uploader";
my $max_size = 5000000;

# Get the sessionId or create a new one
# do not use CGI here, because we need to handle STDIN in order to update the progress status.
my $sid = new Digest::MD5()->add( $$, time(), rand(time) )->hexdigest();
if ( $ENV{'HTTP_COOKIE'} =~ /$idname="*([^";]+)/ ) {
    $sid = $1;
}
my $user_dir = "$tmp_dir/$sid/";

# Controller:
#   POST is used for uploading.
#   GET is used to get the upload progress or get the content of the uploaded item.
my $method = $ENV{'REQUEST_METHOD'} || 'GET';
my $cgi;
if ( $method =~ /POST/i ) {
    doPost();
} else {
    $cgi = new CGI;
    print STDERR ">>>> " . Dumper($cgi->param);
    if ( $cgi->param('show') ) {
        writeItemContent( $cgi->param('show') );
    } elsif ( $cgi->param('remove') ) {
        removeItem( $cgi->param('remove') );
    } elsif ( $cgi->param('cancel') ) {
        cancelProcess();
        writeResponse("<canceled>true</canceled>");
    } else {
        writeResponse( getProgress() );
    }
}
exit;

## This method receives the form content and stores each item in a temporary folder.
sub doPost {
    $| = 1;

    ## Validate request size
    my $len = $ENV{'CONTENT_LENGTH'} || 3000;
    if ( $len && $len > $max_size ) {
        writeResponse("<error>The maximum upload size has been exceeded</error>");
    }

    ## Validate permissions
    if ( !-d "$user_dir" ) {
        mkpath( "$user_dir", 0, 0777 )
          || writeResponse("<error>Unable to create: $user_dir $!</error>");
        chmod( 0777, "$user_dir" );
    }

    ## Receive the request, and update progress data
    my $data_file = "$user_dir/data.$$";
    open( P, ">$data_file" )
      || writeResponse("<error>Can't open postfile: $user_dir/postdata $!</error>");
    my ( $n, $done, $line ) = ( 0, 0, "" );
    do {
        $done += $n;
        updateProgress( $done, $len );
        print P $line;
        select( undef, undef, undef, 0.3 );
    } while ( ( $n = read( STDIN, $line, 4096 ) ) > 0 );
    close(P);

    ## Process received data
    my $msg = "OK\n";
    open( STDIN, "$data_file" );
    $cgi = new CGI();
    foreach my $key ( $cgi->param() ) {
        my $value = $cgi->param($key);
        if ( defined($value) ) {
            my $fh = $cgi->upload($key);
            if ( defined($fh) ) {
                my $type = $cgi->uploadInfo($value)->{'Content-Type'}
                  || 'unknown';
                my $name = saveFile( $key, $value, $type, $fh );
                my $size = -s $name;
                $msg .= "<file><name>$key</name><value>$value</value>"
                  . "<size>$size</size><type>$type</type></file>\n";
            } else {
                $msg .= "<parameter><name>$key</name>"
                  . "<value>$value</value></parameter>\n";
            }
        }
    }
    unlink($data_file);
    writeResponse($msg);
}

## Save each received file in the user folder.
## It generates two files, one has the content of the uploaded data i
## and one with the item information (original name and content-type)
sub saveFile {
    my ( $key, $name, $type, $fd ) = @_;
    $name =~ s/[^\w]/_/g;
    my $bin_file = $user_dir . $key . ".bin";
    open( BIN, ">$bin_file" ) || return;
    while (<$fd>) {
        print BIN $_;
    }
    close(BIN);
    my $info_file = $user_dir . $key . ".info";
    open( INFO, ">$info_file" ) || return;
    print INFO "$name\n$type\n";
    close(INFO);
    return $bin_file;
}

## Write the server response for each request.
## This response is a simple xml response easy to handle in the browser side.
## Set the session cookie to the client if it has not been setted yet.
sub writeResponse {
    my $msg = shift;
    close(STDIN);

    $cgi = new CGI() unless($cgi);
    if ( $cgi->cookie($idname) ) {
        print $cgi->header( -type => 'text/plain' );
    } else {
        print $cgi->header( -cookie => $cgi->cookie( $idname => $sid ),
                            -type   => 'text/plain' );
    }
    print "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
      . "\n<response>\n$msg</response>\n";
    exit;
}

## write the upload progress in the status file
sub updateProgress {
    my ( $done, $total ) = @_;
    if (-f "$user_dir/cancel") {
        writeResponse("<canceled>true<canceled><finished>canceled</finished>");
        exit;
    }
    open( F, ">$user_dir/progress" );
    print F "$done/$total";
    close(F);
}

sub cancelProcess {
    open( F, ">$user_dir/cancel" );
    print F "cancel";
    close(F);
}

## read the upload progress from the status file
sub getProgress {
    if ( -f "$user_dir/cancel" ) {
        unlink ("$user_dir/cancel");
        return "<canceled>true<canceled><finished>canceled</finished>";
    }
    my ( $done, $total, $percent ) = ( 0, 0, 0 );
    if ( open( F, "$user_dir/progress" ) ) {
        my $l = <F>;
        if ( $l =~ /^(\d+)\/(\d+)/ ) {
            ( $done, $total ) = ( $1, $2 );
            $percent = $total != 0 ? $done * 100 / $total : 0;
        }
        close(F);
    }
    my $ret =
        "<percent>$percent</percent>"
      . "<currentBytes>$done</currentBytes>"
      . "<totalBytes>$total</totalBytes>";
    $ret .= "<finished>ok</finished>" if ( $percent >= 100 );
    return $ret;
}

## Generates the response when the client asks for an item
## if it is a form item it returns a xml response with its value
## if it is an uploaded file it returns the content of this file, setting the content-type to the original value.
sub writeItemContent {
    select( undef, undef, undef, 0.4 );
    my $item = shift;
    if ( open( F, "$user_dir/$item.info" ) ) {
        my $value = <F>;
        $value =~ s/[\r\n]+$//g;
        my $type = <F>;
        $type =~ s/[\r\n]+$//g;
        close(F);
        if ( open( F, "$user_dir/$item.bin" ) ) {
            print "Content-type: $type\n\n";
            while (<F>) {
                print $_;
            }
            close(F);
        } else {
            writeResponse("<item name='$item'>$value</item>");
        }
    } else {
        writeResponse("<error>item not found</error>");
    }
}

sub removeItem {
    my $item = shift;
    if (-f "$user_dir/$item.info") {
        unlink ("$user_dir/$item.info");
        unlink ("$user_dir/$item.bin");
        writeResponse("<deleted>true</deleted>");
    } else {
        writeResponse("<error>item not found</error>");
    }
}

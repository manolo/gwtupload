#!/usr/bin/perl
use strict;
use warnings;

sub restoreFile {
    my $file = shift;
    my $bfile = "$file.without.compression";
    if ( -f $bfile ) {
      print "Restored $file ... ";
      system ("cp $bfile $file");
    } else {
      system ("cp $file $bfile");
    }
}


sub readFile {
    my $file = shift;

    my $ret = "";
    my $f;
    if (!open($f, $file)) {
	    print STDERR "Error file: $file ($!)\n";
	    return $ret;
    }
    while(<$f>){
      if (/^\s*\@import\s+url\s*\(\s*\"*([^"]+)\"*\s*\)/) {
	  my $nfile=$1;
	  if ($file =~ /^(.+)\/[^\/]+$/ )  {
		  $nfile="$1/$nfile";
	  }
	  $ret .= " " . readFile($nfile);
      } else {
	  $ret .= $_;
      }
    }
    close($f);
    return $ret;
}

sub JsCompression {
  my $str = shift;
  ($str =~ s#^\s*(/\*.*?\*/)##s or $str =~ s#^\s*(//.*?)\n\s*[^/]##s);
  # removing C/C++ - style comments:
  #$str =~ s#/\*[^*]*\*+([^/*][^*]*\*+)*/|//[^\n]*|("(\\.|[^"\\])*"|'(\\.|[^'\\])*'|.[^/"'\\]*)#$2#gs;
  # remove C-style comments
  $str =~ s#/\*.*?\*/##gs;
  # remove C++-style comments
  $str =~ s#//.*?\n##gs;
  # removing leading/trailing whitespace:
  $str =~ s#(?:(?:^|\n)\s+|\s+(?:$|\n))##gs;
  # removing newlines:
  $str =~ s#\r?\n##gs;
  # condensig whitespaces
  $str =~ s/\s+/ /gs;
  $str =~ s#\s+([:,])#$1#gs;
  $str =~ s#([:,])\s+#$1#gs;
  $str =~ s#\s+\{#\{#gs;
  $str =~ s#\{\s+#\{#gs;

  return $str;
}

sub processFile {
   my ($ifile, $suffix) = @_;
   my $ofile;
  
   if (!$suffix) {
     restoreFile($ifile);
     $ofile=$ifile;
   } elsif ($ifile =~ /^(.*)(\.css)$/) {
     $ofile = $1 . $suffix . $2;
   } else {
     die "File: $ifile, is not a .css file";
   } 

   die "Unable to read file: $ifile" if (! -f $ifile);
   print "Processing: $ifile ... ";

   my $txt = readFile($ifile);
   my $lb = length($txt);
   $txt = JsCompression($txt);
   my $la = length($txt);
   my $p = int($la * 100 / $lb);

   print " Before: $lb bytes. After: $la bytes. ($p %)\n";

   print "Writing $ofile \n";
   open(F,">$ofile") || die $!;
   print F $txt;
}

### MAIN
my ($file, $suffix);
while ($file = shift) {
    if ($file =~ /^-[^\/]+/) {
       $suffix = $file;
       next;
    }
    processFile($file,$suffix);
}

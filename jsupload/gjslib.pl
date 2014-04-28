#!/usr/bin/perl

use Data::Dumper;
use strict;
use File::Basename;

###
###  This 'ugly' perl script creates the wiki page for JsUpload
###  It inspects the java classes looking for comments and put them in the wiki page.
###

my $author = "[http://manolocarrasco.blogspot.com/ Manuel Carrasco Moñino]";
# Folder with source java files
my $path = "src/main/java/jsupload/client/";
# Java classes to inspect
my @classes = ('Upload', 'PreloadImage');
# Class with static constants
my $constants = 'Const';
# Html file with javascript sample code
my $htmlsample = "src/main/java/jsupload/public/JsUpload.html";
# Cgi-bin script
my $cgifile = "src/main/java/jsupload/public/jsupload.cgi.pl";
# Php script
my $phpfile = "src/main/java/jsupload/public/jsupload.php";
# Location of the sample aplication
my $sample_location = "http://gwtupload.alcala.org/gupld/jsupload/JsUpload.html";
# Wiki template with library description
my $wikitpl = "src/main/java/jsupload/public/JsUpload.wiki.txt";
# Output wiki page
my $wikiout = "target/gwtupload.wiki/JsUpload_Documentation.wiki";

######## MAIN

my %const = processConst($constants);
my $txt = docheader();
$txt .= "= Library API =\n";
$txt .= printConst("Const");
foreach my $cl (@classes) {
   $txt .= printClass($cl, processFile($cl));
}
$txt .= doccgi($cgifile);
$txt .= doccgi($phpfile);
$txt .= docsample();
$txt .= "*Author:* _" . $author. "_\n\n";
my $date = `date`;
$date =~ s/[\r\n]+//g;
$txt .= "*Date:* _${date}_\n\n";
$txt .= "This documentation has been generated automatically parsing comments in java files, if you realise any error, please report it\n\n";
$txt =~ s/,(\s*\/\/[^\n]*\n\s*\})/$1/sg;
$txt =~ s/,(\s*\n\s*\})/$1/sg;

open(O, "> $wikiout") || die $!;
print O $txt;
close(O);
print "Generated: $wikiout file\n";
exit;
########

sub processConst {
  my $class = shift;
  my %c;
  $c{class} = $class;

  my $file=$path . $class . ".java";
  open(F, "$file") || die $!;
  my $on = 0;
  my $com = "";
  while(<F>) {
   if (/\s*\/\*\*/) {
      $on = 1;
      $com = "";
   }
   if ($on && /\s*\*\s+(.*)$/) {
      $com .= $1 . "\n";
   }
   $on = 0 if (/\s*\*\//);
   if (/^\s*public\s+class\s+$class/) {
      $c{desc} = $com;
      $com = ""; 
   }
   if ( /(protected|private)\s+.*String.*\s+(T[XI]T_[A-Z]+).*\s+=\s+"([^\"]+).*$/ ) {
      my ($nam, $def) = ($3);
      if ($com eq '' && /\/\/\s*\((.+)\)\s*\[(.+)\]\s*(.*)\s*$/) {
          $def = $2;
          $com = $3;
          my $c = $1;
          foreach (split(/[ ,]+/, $c)) {
            $com = "// $com" if ($com ne '');
            $c{const}{$_}{regional} .= "        $nam: $def, $com\n";
          }
      }
      $com = "";
   }
   elsif (/^\s*(public|protected)\s+.*static\s+.*\s+([A-Z_]+)\s*=/) {
   
      my ($type, $const, $nam, $def) = ($1, $2, "", "");
      
      if ( /\s*=\s*\"([^"]+)/ ) {
         $nam = $1;
      }
      if ($com eq '' && /^.*\/\/(.+)$/){
         $com=$1;
      }
      $com =~ s/^[\s\r\n]+//g;
      $com =~ s/[\s\r\n]+$//g;
      if ($com =~ /^ *\[(.+)\] *(.+)/s){
         $com=$2;
         $def=$1;
         $com =~ s/\n/\n                                \/\/  /sg;
      }
      next if ($com =~ /\@not/);
      $c{const}{$const}{nam}=$nam;
      $c{const}{$const}{def}=$def;
      $c{const}{$const}{com}=$com;
      $c{const}{$const}{type}=$type;
      $com="";
   }
  }
  close(F);
  return %c;
}


sub processFile {
  my $class = shift;
  my %c;
  my $file=$path . $class . ".java";
  open(F, "$file") || die $!;
  my $on = 0;
  my $com = "";
  while(<F>) {
   if ($on && /\s*\*\s+(.*)$/ && !/\@author/) {
      $com .= $1 . "\n";
   }
   if (/\s*\/\*\*/) {
      $on = 1;
      $com = "";
   }
   $on = 0 if (/\s*\*\//);
   if (/^\s*public\s+class\s+$class\s*/) {
      $c{desc} = $com;
      $com = "";
   }
   if (/^\s*public\s$class\s*\(\s*JavaScriptObject/) {
      $c{hasconstructor} = 1;
      $c{desc} = $com if (! $c{desc});
      $com = "";
   }
   elsif (/\s*public\s+(.+)\s+(\w+)\s*\((.*)\)/) {
      my ($ret, $func, $arg) = ($1, $2, $3);
      my $set = ($ret =~ s/static\s+// ) ? "static" : "public";
      next if ($com =~ /\@not/);
      next if ($func =~ /^(onChange|onClick|run)$/);
      my $idx = "$func . $arg";
      $c{$set}{$idx}{func} = $func;
      $c{$set}{$idx}{ret} = $ret;
      $c{$set}{$idx}{arg} = $arg;
      $com =~ s/[\r\n]+$//;
      $com =~ s/^[\r\n]+//;
      $c{$set}{$idx}{com} = $com;
      $com="";
   } #public static native String
   if (/Const\.([A-Z_]+)/) {
       next if (defined $c{const} && $c{const} =~ / $1 /);
       #$c{const} .= " " if (defined $c{const});
       $c{const} .=  " $1 ";
   }
  }
  close(F);
  return %c;
}

sub printConst {
  my ($class) = @_;
  my $ret = "";
  my $tmp = $const{const};
  foreach (keys %$tmp) {
     if ($const{const}{$_}{type} eq 'public') {
        $ret .= "jsc.$class.$_ //" . $const{const}{$_}{com} . "\n";
     }
  }
  return ($ret ne '') ? "\n== $class ==\n_$const{desc}_\n   * Public static constants\n{{{\n$ret}}}\n" : "";
}

sub printClass {
  my ($name, %c) = @_;
  my $desc = $c{desc};
  $desc =~ s/^[\r\n]+//g;
  $desc =~ s/[\r\n]+/\n    /g;
  
  my $ret = unc("\n== $name ==\n_${desc}_\n");
  
  $ret .= printConstructor($name, %c) if ($c{hasconstructor});
  
  my $a = $c{public};
  my $t = "";
  foreach my $idx (keys %$a) {
     $t .= printMethod(1, $name, $idx, %c);
  }
  $ret .= "   * Instance methods\n{{{\n$t}}}\n" if ($t ne '');

  $a = $c{static};
  $t = "";
  foreach my $idx (keys %$a) {
     $t .= printMethod(0, $name, $idx, %c);
  }
  
  $ret .=  "   * Static methods\n{{{\n$t}}}\n" if ($t ne '');
  return $ret;
}

sub printConstructor {
  my ($name, %c) = @_;

  my $var = lc($name);
  my $ret = "";

  $ret .=  "   * Constructor\n";
  $ret .=  "{{{\nvar $var = new jsc.$name ({\n";

  foreach (split(/\s+/, $c{const})) {
   my $d = $_;
   next if ($d eq '' || $d =~ /^\s+$/);
   if ($d =~ /regional/i) {
     $ret .=  "   " . $const{const}{$d}{nam} . ": {     // " . $const{const}{$d}{com} . "\n" . $const{const}{$name}{regional} . "   },\n"
   } elsif ($const{const}{$d}{nam}) {
     $ret .=  "   " . $const{const}{$d}{nam} . ": " . $const{const}{$d}{def} . ",  // " . $const{const}{$d}{com} . "\n";
   }
  }

  $ret .=  "});\n}}}\n";
  return $ret;
}

sub unc {
  my $w = shift;
  $w =~ s/([a-z])([A-Z])/"$1 ".uc($2)/eg;
  return $w
}

sub printMethod {
      my ($public, $name, $idx, %c) = @_;
      my $var = $public ? lc($name) : "jsc.$name";
      my $set = $public ? "public": "static";
      my $func = $c{$set}{$idx}{func};
      my $ret = $c{$set}{$idx}{ret};
      my $arg = $c{$set}{$idx}{arg};
      my $com = $c{$set}{$idx}{com};
      my $args = "";
      if ($arg && $arg ne '') {
         foreach my $a (split(/\s*,\s*/, $arg)) {
             if ($a =~ /^\w+\s+(.+)$/) {
                $args .= "$1, ";
             }
         }
         $args =~ s/[, ]+$//;
      }
      $ret =~ s/^.*\s+(\w+)\s*$/$1/g;
      my $rets = "";
      if ($ret ne 'void') {
         my $vname = lc($ret);
         $vname = "date" if ($vname eq 'javascriptobject' && $func !~ /data/i);
         $rets = "var a_" . $vname . " = ";
      }
     if ($com =~ /\n/) {
        $com = "/* $com */"
     } else {
        $com = "/* $com */"
     }
     return "\n$com\n$rets$var.$func($args);\n";
}

sub docheader {
  my $ret = "";	
  open(W, $wikitpl) || die $! . " $wikitpl";
  while(<W>){
  	$ret .= $_;
  }	
  close(W);
  return $ret;
}

sub docsample {
   my $ret = "";
   open(F, $htmlsample) || die $! . " $htmlsample" ;
   my $on = 0;
   while(<F>) {
      $on = 0 if (/<\/script/);
      $ret .= $_ if ($on);
      $on = 1 if (/<script\s+id='jsexample'>/);
   }
   close(F);
   $ret = "= Sample Code =\nYou can view this in action in the example  [$sample_location here]\n{{{\n$ret\n}}}\n";
   return $ret;
}

sub doccgi {
   my $file = shift;
   my $ret = "";
   my $in = 0;
   open(F, $file) || die $! . " $file" ;
   while(<F>) {
          if (/^## \*(\s*.*)\s*$/) {
            my $l = $1;
            $in = 1 if ($l =~ /{{{$/);
            $ret .= "\n"     if ($ret =~ /[\.\:]$/ );
            $ret .= "\n  "   if ($l =~ /^[#\*] /);
            $l = "\n$l\n" if ($in);
            $ret .= "$l";
            $in = 0 if ($l =~ /}}}$/);
          }
   }
   close(F);
   $ret =~ s/\n+/\n/g;
   my $name = basename($file);
   $ret = "= Server script ($name) =\n$ret\n";
   return $ret;
}
exit;



Cp() {
  mv target/site .
  mv core/target/site site/gwtupload
  mv gae/target/site site/gwtupload-gae
  mv jsupload/target/site site/jsupload
  mv samples/target/site site/gwtupload-samples
}



site=mvn-site
svn co https://gwtupload.googlecode.com/svn/site target/$site
find target/$site -type f -name "*html" | xargs svn propset svn:mime-type text/html
find target/$site -type f -name "*css" | xargs svn propset svn:mime-type text/css
find target/$site -type f -name "*html" -exec perl -pi -e 's#size=.\+2.#class="NavBarFont1Rev"#gi' '{}' ';'
for i in ./ gwtupload gwtupload-gae jsupload gwtupload-gae
do
  cp javadoc/inherit.gif target/$site/$i/apidocs/resources/
  cp javadoc/stylesheet.css target/$site/$i/apidocs/
  cp javadoc/javadoc.jpg target/$site/$i/apidocs/
  svn add target/$site/$i/apidocs/javadoc.jpg
done
svn commit -m 'changed filetype to html files' target/$site




svn co https://gwtupload.googlecode.com/svn/site/gwtupload-gae target/gwtupload-gae
find target/gwtupload-gae -type f -name "*html" | xargs svn propset svn:mime-type text/html
find target/gwtupload-gae -type f -name "*css" | xargs svn propset svn:mime-type text/css
find target/gwtupload-gae -type f -name "*html" -exec perl -pi -e 's#size=.\+2.#class="NavBarFont1Rev"#gi' '{}' ';'
cp javadoc/inherit.gif target/gwtupload-gae/apidocs/resources/
cp javadoc/stylesheet.css target/gwtupload-gae/apidocs/
cp javadoc/javadoc.jpg target/gwtupload-gae/apidocs/
svn add target/gwtupload-gae/apidocs/javadoc.jpg
svn commit -m 'changed filetype to html files' target/gwtupload-gae


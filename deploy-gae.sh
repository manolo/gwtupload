D=`pwd`


cd $D/core
mvn clean install deploy
mvn gcupload:gcupload
cd $D/gae
mvn clean install deploy
cd $D/../GWTUpload-mavenrepo
svn commit -m 'updated maven snapshots'
cd $D/samples-gae
mvn clean deploy


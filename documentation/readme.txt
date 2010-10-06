Deploying the plugin with maven
1) Edit the pom.xml and set the wanted release version.
2) Commit all changes to repository --> If there are local changes, Maven Release will get an error and suspended. 
3) mvn release:clean (otpional)
4) mvn release:prepare  
5) mvn release:perform
6) The destination URL is specified in the pom.xml: 
	<distributionManagement>
		<repository>
			<id>CDKTaverna2-repository</id>
			<name>CDK-Taverna 2 Repository</name>
			<url>file:///P:/CDKTaverna/release_repository</url>
		</repository>
	</distributionManagement>
7) An the repository under:
	<scm>
		<connection>scm:svn:https://cdktaverna2.svn.sourceforge.net/svnroot/cdktaverna2/tags/</connection>
		<developerConnection>scm:svn:https://cdktaverna2.svn.sourceforge.net/svnroot/cdktaverna2/tags/</developerConnection>
 	</scm>

Debugging maven with eclipse:
1)  mvn -Dmaven.surefire.debug -Dtest=??? test
2)  eclipse: debug-> debug configurations -> remote java application
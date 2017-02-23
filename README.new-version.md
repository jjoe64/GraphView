How to create a new version for maven repo
--------------------------------------------
create sources.jar
- $ jar cvf sources.jar src

create java doc jar
- $ mkdir javadoc
- $ javadoc -d javadoc -sourcepath src/main/java/ -subpackages com.jjoe64
- $ jar cvf javadoc.jar javadoc

change version in gradle.properties

uncomment part for publishing in build.gradle

(once) create a gpg file
- gpg --gen-key

(once) publish key
- gpg --send-keys D8C3B041
and/or here as ascii
- gpg --export -a D8C3B041
- http://keyserver.ubuntu.com:11371/

=> needs some time

hardcode user/pwd of nexus account in maven_push.gradle

success gradle task uploadArchives
-  ./gradlew --rerun-tasks uploadArchives
- enter gpg info (id:D8C3B041 / path: /Users/jonas/.gnupg/secring.gpg / PWD)

open https://oss.sonatype.org

login

Staging Repositiories

search: jjoe64

Close entry

Refresh/Wait

Release entry

Wait some days

How to create a new .jar file
--------------------------------
success this gradle task
- ./gradlew --rerun-tasks clearJar makeJar
- cp build/outputs/myCompiledLibrary.jar public/GraphView-4.2.0.jar

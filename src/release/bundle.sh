# bootstrapping needs extra memory
export MAVEN_OPTS=-Xmx512M

# create the bundle and then the assemblies
mvn clean package info.magnolia:maven-bundle-plugin:bundle assembly:assembly
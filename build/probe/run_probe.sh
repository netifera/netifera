#!/bin/sh

OSGI_BUNDLE=org.eclipse.osgi_3.4.3.R34x_v20081215-1030.jar
OSGI_PATH=../plugins/${OSGI_BUNDLE}
OSGI_ARGUMENTS="-noExit -console -configuration configuration"

if [ -x ../jre/bin/java ] ; then
	JAVA_PATH=../jre/bin/java
else
	JAVA_PATH=`which java`
fi

echo "Java path is ${JAVA_PATH}"

${JAVA_PATH} -jar ${OSGI_PATH} ${OSGI_ARGUMENTS}


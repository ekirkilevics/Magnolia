===============================================

              M A G N O L I A
          Content Management Suite
		Build 1074

              Jun 17th 2005
           Release Notes v 2.1 RC2 

          http://www.magnolia.info/



===============================================
Overview
===============================================

Welcome to Magnolia 2.1

These release notes contain information that 
should help getting you up & running with Magnolia

===============================================
Changes, information and bugs
===============================================

All necessary changes and Information are 
written on.

http://www.magnolia.info/en/magnolia/developer/issues.html

===============================================
Magnolia Package
===============================================
The Package contains:
 
 Apache Tomcat 5.0.28
 Magnolia 2.1 RC2
 Samples

=============================================== 
System Requirements
===============================================

To run Magnolia, all you need is Java 1.4.2.x
We bundle tomcat as a J2EE application server for
your convenience but other J2EE servers should 
work as well.

The authoring environment will need a browser. The
latest list of tested and compliant browsers can 
be found at www.magnolia.info


=============================================== 
Installation & setup:  starting Magnolia
===============================================

After unzipping, you will have to start the two
Magnolia instances in one step

1. Starting the Instances

1.1. Unix (all flavors)

  open a shell and set the environment variables
  JAVA_HOME and preferably CATALINA_HOME. If you
  do not set CATALINA_HOME, you will have to 
  start Magnolia from within the bin/ directory
 
    > sh
    > cd INSTALL_DIRECTORY/tomcat/bin
    > export JAVA_HOME=YOUR_JAVA1.4_HOME
    > ./startup.sh


  Here is an example of a small shell script 
  that you could use to start the authoring instance
 
  --------- 8< ---------
  #!/bin/sh
  #
  # start admin instance
  #
  export JAVA_HOME=/Library/Java/Home
  cd INSTALL_DIRECTORY/tomcat/bin
  ./startup.sh
  --------- 8< ---------


1.2. Microsoft Windows

  open command prompt 
    > C:\set JAVA_HOME=YOUR_JAVA1.4_HOME
    > C:\INSTALL_DIRECTORY\tomcat\bin\startup.bat


=============================================== 
Connecting to Magnolia
===============================================Ê

Note : Authoring and Public instance must be 
running before you can access these URL's
this will take a few minutes to initialize 
the repositories.

To start using the Magnolia public environment, 
open a browser and goto 
http://localhost:8081/magnoliaPublic

To edit configuration on public instance go to
http://localhost:8081/magnoliaPublic/.magnolia and login as 
superuser.

To start using the Magnolia authoring environment, 
open a browser and goto 
http://localhost:8081/magnoliaAuthor

The default username & password are:

- username : superuser
- password : superuser


=============================================== 
Known Issues in Magnolia 2.1 RC2
=============================================== 

http://www.magnolia.info/en/magnolia/developer/issues.html

=============================================== 
Documentation & Support
===============================================

You can download all documentation at:

	http://www.magnolia.info/

THIS SOFTWARE IS PROVIDED "AS-IS" AND FREE OF
CHARGE, WITH ABSOLUTELY NO WARRANTY OR SUPPORT OF
ANY KIND, EXPRESSED OR IMPLIED, UNDER THE TERMS OF
THE INCLUDED LICENSE AGREEMENT.

Free help is available at the community 
mailing-list (http://www.magnolia.info/en/magnolia/developer/user-list.html) 
on a volunteer basis from members of the community and the 
obinary ltd. staff. Please submit help requests 
and bug reports concerning our free software 
distributions to the mailing-list. 

For email and phone support, commercial support 
packages are available from obinary ltd. See 
http://www.magnolia.info/en/magnolia/services.html
for details on our commercial services regarding
Magnolia


Thanks for using Magnolia 2.1 RC2

obinary ltd.
marketing@magnolia.info

Copyright 1993-2004 obinary ltd. 

http://www.magnolia.info All rights reserved.
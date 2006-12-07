@echo off
rem -- the $JDKPath variable can be replaced by external tools such as an installer
if exist "$JDKPath" set JAVA_HOME=$JDKPath

if ""%1"" == ""start"" goto doStart
if ""%1"" == ""stop"" goto doStop
goto noCommand

:doStart
rem Magnolia needs extra memory
set CATALINA_OPTS=%CATALINA_OPTS% -Xms64M -Xmx512M -Djava.awt.headless=true
call startup.bat
goto end

:doStop
call shutdown.bat
goto end

:noCommand
echo Please provide "start" or "stop" as argument.

:end
pause

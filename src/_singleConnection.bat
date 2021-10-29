@echo off

echo compiling:
javac -encoding UTF-8 Server1.java
echo run:
start java Server1

echo compiling:
javac -encoding UTF-8 Client.java
echo run:
start java Client

pause
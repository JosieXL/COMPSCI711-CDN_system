@echo off

echo compiling:
javac -encoding UTF-8 ServerCDN.java
echo run:
start java ServerCDN

echo compiling:
javac -encoding UTF-8 Cache.java
echo run:
start java Cache

echo compiling:
javac -encoding UTF-8 ClientCDN.java
echo run:
start java ClientCDN


pause
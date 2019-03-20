#!/bin/bash
rm user*
rm *.class
javac RSAKeyGenerator.java

for i in $(seq "$1"); do
  java RSAKeyGenerator w "user$i" "user$i.pub"
  #ssh-keygen -t rsa -f "user$i" -N ""
done
#ssh-keygen -t rsa -f user0

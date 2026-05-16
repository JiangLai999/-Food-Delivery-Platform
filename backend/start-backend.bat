@echo off
setlocal
echo Starting backend from repository root...
cd /d "%~dp0" 
cd ..
mvn -f backend/pom.xml clean package -DskipTests
mvn -f backend/pom.xml spring-boot:run

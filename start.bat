@echo off
for %%# in (target/dotwebstack-theatre-legacy*.jar) do set "jar=%%~nx#"
java -jar target/%jar%
pause

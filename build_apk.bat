@echo off
cd /d "%~dp0"
call gradlew.bat --no-daemon assembleDebug
if exist app\build\outputs\apk\debug\app-debug.apk copy /Y app\build\outputs\apk\debug\app-debug.apk HWT_Maker_GT6.apk

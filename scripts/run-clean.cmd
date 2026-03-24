@echo off
setlocal

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-clean.ps1" %*
exit /b %errorlevel%

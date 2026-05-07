@echo off
echo ====================================================
echo      CAREENEST - CLOUDFLARE TUNNEL (PORT 8080)
echo ====================================================
echo.
echo Dang khoi tao duong link Public cho Backend...
echo Vui long cho trong giay lat va copy duong link co duoi ".trycloudflare.com"
echo de paste vao file .env cua Frontend.
echo.
cloudflared tunnel --url http://localhost:8080
pause

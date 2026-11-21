<#
.SYNOPSIS
    Testy API LostLink: rejestracja, logowanie, token, uprawnienia i akcje admina.

.DESCRIPTION
    Wysyła żądania do działającej aplikacji i sprawdza kody odpowiedzi.
    Dane admina czyta z pliku .env (ADMIN_EMAIL, ADMIN_PASSWORD).
    Każde uruchomienie tworzy nowego użytkownika testowego (e-mail z godziną) i na końcu go usuwa.

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File tests\api-test.ps1

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File tests\api-test.ps1 -BaseUrl http://localhost:9090
#>
param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$EnvFile = (Join-Path $PSScriptRoot "..\.env")
)

$ErrorActionPreference = "Stop"
$script:passed = 0
$script:failed = 0

# ---------------------------------------------------------------------------
# Funkcje pomocnicze
# ---------------------------------------------------------------------------

function Read-EnvFile([string]$Path) {
    if (-not (Test-Path $Path)) {
        throw "Nie znaleziono pliku .env: $Path"
    }
    $values = @{}
    foreach ($line in Get-Content $Path) {
        if ($line -match '^\s*([A-Z_][A-Z0-9_]*)\s*=\s*(.*)$') {
            $values[$Matches[1]] = $Matches[2].Trim().Trim('"')
        }
    }
    return $values
}

# Wysyła żądanie i zwraca { Status, Text, Json }. Body podajesz jako hashtable.
function Invoke-Api {
    param([string]$Method, [string]$Path, $Body = $null, [string]$Token = $null)

    $responseFile = New-TemporaryFile
    $requestFile = $null
    $curlArgs = @("-s", "-o", $responseFile.FullName, "-w", "%{http_code}", "-X", $Method, "$BaseUrl$Path")

    if ($Token) {
        $curlArgs += @("-H", "Authorization: Bearer $Token")
    }
    if ($null -ne $Body) {
        $requestFile = New-TemporaryFile
        ($Body | ConvertTo-Json -Compress) | Set-Content -Path $requestFile.FullName -NoNewline -Encoding UTF8
        $curlArgs += @("-H", "Content-Type: application/json", "--data-binary", "@$($requestFile.FullName)")
    }

    try {
        $status = [int](curl.exe @curlArgs)
        $text = Get-Content -Path $responseFile.FullName -Raw -Encoding UTF8
        $json = $null
        if ($text) {
            try { $json = $text | ConvertFrom-Json } catch { $json = $null }
        }
        return [pscustomobject]@{ Status = $status; Text = $text; Json = $json }
    }
    finally {
        Remove-Item $responseFile.FullName
        if ($requestFile) { Remove-Item $requestFile.FullName }
    }
}

# Wysyła żądanie, porównuje status z oczekiwanym i wypisuje [OK] / [FAIL].
# Z -PassThru zwraca odpowiedź (np. żeby wyciągnąć token).
function Test-Api {
    param(
        [string]$Name,
        [int]$Expected,
        [string]$Method,
        [string]$Path,
        $Body = $null,
        [string]$Token = $null,
        [switch]$PassThru
    )

    $response = Invoke-Api -Method $Method -Path $Path -Body $Body -Token $Token

    if ($response.Status -eq $Expected) {
        $script:passed++
        Write-Host ("  [OK]   {0,-60} {1}" -f $Name, $response.Status) -ForegroundColor Green
    }
    else {
        $script:failed++
        Write-Host ("  [FAIL] {0,-60} oczekiwano {1}, jest {2}" -f $Name, $Expected, $response.Status) -ForegroundColor Red
        if ($response.Text) {
            Write-Host "         $($response.Text)" -ForegroundColor DarkGray
        }
    }

    if ($PassThru) { return $response }
}

function Section([string]$Title) {
    Write-Host "`n== $Title ==" -ForegroundColor Cyan
}

function Require([object]$Value, [string]$What) {
    if (-not $Value) {
        Write-Host "`nPrzerwano: brak $What - dalsze testy nie mają sensu." -ForegroundColor Red
        exit 1
    }
}

# ---------------------------------------------------------------------------
# Dane testowe
# ---------------------------------------------------------------------------

$envValues = Read-EnvFile $EnvFile
$adminEmail = $envValues["ADMIN_EMAIL"]
$adminPassword = $envValues["ADMIN_PASSWORD"]
Require $adminEmail "ADMIN_EMAIL w .env"
Require $adminPassword "ADMIN_PASSWORD w .env"

$suffix = Get-Date -Format "HHmmss"
$annaEmail = "anna$suffix@test.pl"
$annaPassword = "d"

Write-Host "Testy API: $BaseUrl (użytkownik testowy: $annaEmail)" -ForegroundColor White

# ---------------------------------------------------------------------------
# Testy
# ---------------------------------------------------------------------------

Section "1. Health i nieznany endpoint"
$health = Test-Api "Health" 200 GET "/api/health" -PassThru
Require ($health.Status -eq 200) "działającej aplikacji pod $BaseUrl"
Test-Api "Nieznany endpoint" 404 POST "/api/auth/cokolwiek" @{}

Section "2. Rejestracja"
Test-Api "Zły e-mail"           400 POST "/api/auth/register" @{ email = "zly-email"; password = $annaPassword; displayName = "Anna" }
Test-Api "Za krótkie hasło"     400 POST "/api/auth/register" @{ email = $annaEmail;  password = "krotkie";     displayName = "Anna" }
Test-Api "Pusta nazwa"          400 POST "/api/auth/register" @{ email = $annaEmail;  password = $annaPassword; displayName = "" }
Test-Api "Poprawna rejestracja" 201 POST "/api/auth/register" @{ email = $annaEmail;  password = $annaPassword; displayName = "Anna" }
Test-Api "Zajęty e-mail"        409 POST "/api/auth/register" @{ email = $annaEmail;  password = $annaPassword; displayName = "Anna" }

Section "3. Logowanie"
Test-Api "Złe hasło"            401 POST "/api/auth/login" @{ email = $annaEmail;  password = "zlehaslo12345" }
Test-Api "Nieistniejący e-mail" 401 POST "/api/auth/login" @{ email = "nie@ma.pl"; password = "cokolwiek1234" }
$anna  = (Test-Api "Logowanie Anny"  200 POST "/api/auth/login" @{ email = $annaEmail;  password = $annaPassword }  -PassThru).Json.token
$admin = (Test-Api "Logowanie admina" 200 POST "/api/auth/login" @{ email = $adminEmail; password = $adminPassword } -PassThru).Json.token
Require $anna "tokenu Anny"
Require $admin "tokenu admina"

Section "4. Token i /me"
Test-Api "Me z tokenem"   200 GET "/api/users/me" -Token $anna
Test-Api "Me bez tokenu"  401 GET "/api/users/me"
Test-Api "Me ze złym tokenem" 401 GET "/api/users/me" -Token "zly.token"

Section "5. Lista użytkowników"
Test-Api "Lista jako user" 403 GET "/api/users" -Token $anna
$users = (Test-Api "Lista jako admin" 200 GET "/api/users" -Token $admin -PassThru).Json
$annaId  = ($users | Where-Object email -eq $annaEmail).id
$adminId = ($users | Where-Object email -eq $adminEmail).id
Require $annaId "id Anny na liście"
Require $adminId "id admina na liście"

Section "6. Admin nie może działać na sobie"
Test-Api "Blokada samego siebie"     403 POST "/api/users/block"  @{ id = $adminId; blocked = $true } -Token $admin
Test-Api "Zmiana własnej roli"       403 POST "/api/users/role"   @{ id = $adminId; role = "USER" }   -Token $admin
Test-Api "Usunięcie samego siebie"   403 POST "/api/users/delete" @{ id = $adminId }                  -Token $admin

Section "7. Blokowanie"
Test-Api "Zablokowanie Anny"               200 POST "/api/users/block" @{ id = $annaId; blocked = $true }  -Token $admin
Test-Api "Me zablokowanej (stary token)"   403 GET  "/api/users/me" -Token $anna
Test-Api "Logowanie zablokowanej"          403 POST "/api/auth/login" @{ email = $annaEmail; password = $annaPassword }
Test-Api "Odblokowanie Anny"               200 POST "/api/users/block" @{ id = $annaId; blocked = $false } -Token $admin
Test-Api "Me po odblokowaniu"              200 GET  "/api/users/me" -Token $anna

Section "8. Zmiana roli"
Test-Api "Anna -> ADMIN"           200 POST "/api/users/role" @{ id = $annaId; role = "ADMIN" } -Token $admin
Test-Api "Anna widzi listę"        200 GET  "/api/users" -Token $anna
Test-Api "Anna -> USER"            200 POST "/api/users/role" @{ id = $annaId; role = "USER" }  -Token $admin
Test-Api "Anna znowu bez listy"    403 GET  "/api/users" -Token $anna

Section "9. Złe dane w body"
Test-Api "Brak pola blocked"       400 POST "/api/users/block" @{ id = $annaId }                                    -Token $admin
Test-Api "Brak pola id"            400 POST "/api/users/block" @{ blocked = $true }                                 -Token $admin
Test-Api "Nieznana rola"           400 POST "/api/users/role"  @{ id = $annaId; role = "SUPERADMIN" }               -Token $admin
Test-Api "Zły format UUID"         400 POST "/api/users/block" @{ id = "to-nie-uuid"; blocked = $true }             -Token $admin
Test-Api "Nieistniejący user"      404 POST "/api/users/block" @{ id = [guid]::NewGuid().ToString(); blocked = $true } -Token $admin

Section "10. Usuwanie"
Test-Api "Usunięcie Anny"          204 POST "/api/users/delete" @{ id = $annaId } -Token $admin
Test-Api "Me usuniętej"            401 GET  "/api/users/me" -Token $anna
Test-Api "Ponowne usunięcie"       404 POST "/api/users/delete" @{ id = $annaId } -Token $admin

# ---------------------------------------------------------------------------
# Podsumowanie
# ---------------------------------------------------------------------------

$total = $script:passed + $script:failed
Write-Host ""
if ($script:failed -eq 0) {
    Write-Host "Wszystkie testy przeszły: $script:passed / $total" -ForegroundColor Green
    exit 0
}
else {
    Write-Host "Nie przeszło: $script:failed / $total" -ForegroundColor Red
    exit 1
}

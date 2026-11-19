# Sobe a API ReAlimenta SP com credenciais Oracle somente na sessao atual.
# Nao grava senha em disco. Execute na raiz do repositorio ou a partir de scripts\.

$ErrorActionPreference = "Stop"
$raiz = Split-Path -Parent $PSScriptRoot
Set-Location $raiz

# JDK 17 no PATH nao compila release 21. Preferir JBR do Android Studio se existir.
$jbr = "C:\Program Files\Android\Android Studio\jbr"
if ((Test-Path "$jbr\bin\java.exe") -and -not $env:JAVA_HOME) {
    $env:JAVA_HOME = $jbr
}

$env:DB_URL = "jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL"
$env:DB_USERNAME = "rm565341"
if (-not $env:PORT) {
    $env:PORT = "8080"
}

if (-not $env:DB_PASSWORD) {
    $cred = Get-Credential -UserName $env:DB_USERNAME -Message "Senha do schema Oracle FIAP (rm565341)"
    $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($cred.Password)
    try {
        $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringAuto($ptr)
    }
    finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
    }
}

& "$raiz\mvnw.cmd" spring-boot:run
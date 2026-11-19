# Consultas somente leitura: MAX(ID) e USER_SEQUENCES. Nao chama NEXTVAL.
# Requer sqlplus no PATH. Nao grava senha em disco.

$ErrorActionPreference = "Stop"

$sqlplus = Get-Command sqlplus -ErrorAction SilentlyContinue
if (-not $sqlplus) {
    Write-Error "sqlplus nao encontrado no PATH. Execute as consultas no SQL Developer (ver README desta pasta)."
    exit 1
}

$usuario = "rm565341"
if (-not $env:DB_PASSWORD) {
    $cred = Get-Credential -UserName $usuario -Message "Senha do schema Oracle FIAP (rm565341)"
    $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($cred.Password)
    try {
        $senha = [Runtime.InteropServices.Marshal]::PtrToStringAuto($ptr)
    }
    finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
    }
} else {
    $senha = $env:DB_PASSWORD
}

$connect = "${usuario}/${senha}@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=oracle.fiap.com.br)(PORT=1521))(CONNECT_DATA=(SID=ORCL)))"
$senha = $null

$consultas = @"
SET PAGESIZE 200 LINESIZE 200 FEEDBACK OFF VERIFY OFF HEADING ON
PROMPT === MAX(ID) por tabela ===
SELECT 'ESTABELECIMENTO' AS TABELA, MAX(ID) AS MAX_ID FROM ESTABELECIMENTO
UNION ALL SELECT 'INSTITUICAO', MAX(ID) FROM INSTITUICAO
UNION ALL SELECT 'LOTE_ALIMENTO', MAX(ID) FROM LOTE_ALIMENTO
UNION ALL SELECT 'DOACAO', MAX(ID) FROM DOACAO;
PROMPT
PROMPT === Contagens (referencia) ===
SELECT 'ESTABELECIMENTO' AS TABELA, COUNT(*) AS QTD FROM ESTABELECIMENTO
UNION ALL SELECT 'INSTITUICAO', COUNT(*) FROM INSTITUICAO
UNION ALL SELECT 'LOTE_ALIMENTO', COUNT(*) FROM LOTE_ALIMENTO
UNION ALL SELECT 'DOACAO', COUNT(*) FROM DOACAO;
PROMPT
PROMPT === USER_SEQUENCES (sem NEXTVAL) ===
SELECT SEQUENCE_NAME, MIN_VALUE, MAX_VALUE, INCREMENT_BY, CACHE_SIZE, LAST_NUMBER, CYCLE_FLAG
FROM USER_SEQUENCES
WHERE SEQUENCE_NAME IN (
  'SEQ_ESTABELECIMENTO', 'SEQ_INSTITUICAO', 'SEQ_LOTE_ALIMENTO', 'SEQ_DOACAO'
)
ORDER BY SEQUENCE_NAME;
EXIT
"@

$consultas | & $sqlplus.Path -S $connect
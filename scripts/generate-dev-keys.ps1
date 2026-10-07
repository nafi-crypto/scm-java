$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$secrets = Join-Path $root "infrastructure\secrets"
$identitySecrets = Join-Path $secrets "identity"
$publicSecrets = Join-Path $secrets "public"
$privateKey = Join-Path $identitySecrets "jwt-private.pem"
$publicKey = Join-Path $publicSecrets "jwt-public.pem"

if (-not (Get-Command openssl -ErrorAction SilentlyContinue)) {
    throw "OpenSSL is required. Install OpenSSL and ensure openssl.exe is on PATH."
}
if ((Test-Path -LiteralPath $privateKey) -or (Test-Path -LiteralPath $publicKey)) {
    throw "A key already exists. Move it aside explicitly before generating a new development pair."
}

New-Item -ItemType Directory -Path $identitySecrets -Force | Out-Null
New-Item -ItemType Directory -Path $publicSecrets -Force | Out-Null
& openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:3072 -out $privateKey
if ($LASTEXITCODE -ne 0) { throw "OpenSSL failed to generate the private key." }
& openssl pkey -in $privateKey -pubout -out $publicKey
if ($LASTEXITCODE -ne 0) { throw "OpenSSL failed to derive the public key." }

Write-Output "Created local-only RS256 development keys under infrastructure\secrets."

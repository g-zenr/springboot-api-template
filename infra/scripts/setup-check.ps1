#Requires -Version 5.1
<#
.SYNOPSIS
  Quick sanity check before local or cloud setup.
#>
$ErrorActionPreference = "Continue"
$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$ok = $true

function Check {
	param(
		[string] $Label,
		[scriptblock] $Test
	)
	try {
		$result = & $Test
		if ($result) {
			Write-Host "[OK]  $Label" -ForegroundColor Green
		}
		else {
			Write-Host "[!!]  $Label" -ForegroundColor Yellow
			$script:ok = $false
		}
	}
	catch {
		Write-Host "[!!]  $Label" -ForegroundColor Yellow
		$script:ok = $false
	}
}

Write-Host "Repo: $repoRoot"
Write-Host ""

Check "Dockerfile present" { Test-Path (Join-Path $repoRoot "Dockerfile") }
Check "docker-compose.yml present" { Test-Path (Join-Path $repoRoot "docker-compose.yml") }
Check "infra/CONTRACT.md present" { Test-Path (Join-Path $repoRoot "infra\CONTRACT.md") }
Check "infra/env.contract.env.example present" { Test-Path (Join-Path $repoRoot "infra\env.contract.env.example") }
Check "infra/SETUP.md present" { Test-Path (Join-Path $repoRoot "infra\SETUP.md") }
Check "AWS stack present" { Test-Path (Join-Path $repoRoot "infra\terraform\aws\versions.tf") }
Check "Azure stub present" { Test-Path (Join-Path $repoRoot "infra\terraform\azure\README.md") }
Check "GCP stub present" { Test-Path (Join-Path $repoRoot "infra\terraform\gcp\README.md") }

Check "Docker CLI" {
	$null = Get-Command docker -ErrorAction Stop
	$true
}
Check "Terraform CLI" {
	$null = Get-Command terraform -ErrorAction Stop
	$true
}

Write-Host ""
if ($ok) {
	Write-Host "All checks passed. See infra/SETUP.md for Path A (local) or Path B (AWS)." -ForegroundColor Green
	exit 0
}

Write-Host "Some checks failed. Install missing tools or restore files, then re-run." -ForegroundColor Yellow
Write-Host "Guide: infra/SETUP.md"
exit 1

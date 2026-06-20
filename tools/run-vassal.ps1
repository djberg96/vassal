param(
  [Parameter(ValueFromRemainingArguments = $true)]
  [string[]] $VassalArgs
)

$ErrorActionPreference = "Stop"

$Root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
Set-Location $Root

if ($env:MVN) {
  $Mvn = $env:MVN
}
elseif (Test-Path (Join-Path $Root "mvnw.cmd")) {
  $Mvn = Join-Path $Root "mvnw.cmd"
}
else {
  $Mvn = "mvn"
}

if ($env:JAVA) {
  $Java = $env:JAVA
}
elseif ($env:JAVA_HOME) {
  $Java = Join-Path $env:JAVA_HOME "bin\java.exe"
}
else {
  $Java = "java"
}

if ($env:MAVEN_THREADS) {
  $MavenThreads = $env:MAVEN_THREADS
}
else {
  $DefaultThreads = [Math]::Max(1, [int]([Environment]::ProcessorCount / 2))
  $MavenThreads = $DefaultThreads.ToString()
}

$ClasspathFile = Join-Path ([System.IO.Path]::GetTempPath()) "vassal-app.classpath"

& $Mvn -T $MavenThreads -U -pl vassal-app -am compile -DskipTests -Dcheckstyle.skip -Dpmd.skip -Dspotbugs.skip
& $Mvn -T $MavenThreads -pl vassal-app dependency:build-classpath "-Dmdep.outputFile=$ClasspathFile"

$DependencyClasspath = (Get-Content -Raw $ClasspathFile).Trim()
$PathSeparator = [System.IO.Path]::PathSeparator
$Classpath = @(
  (Join-Path $Root "vassal-app\target\classes")
  (Join-Path $Root "vassal-deprecation\target\classes")
  $DependencyClasspath
) -join $PathSeparator

& $Java -cp $Classpath VASSAL.launch.ModuleManager @VassalArgs
exit $LASTEXITCODE

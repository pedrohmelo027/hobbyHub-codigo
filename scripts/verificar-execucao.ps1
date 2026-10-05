$ErrorActionPreference = 'Stop'
$taskServer = $null
function Start-HobbyHubSmoke {
 $taskJava = 'C:\Program Files\Java\jdk-21.0.12\bin\java.exe'
 $script:taskServer = Start-Process -FilePath $taskJava -ArgumentList @('-jar','target/hobbyhub-0.0.1-SNAPSHOT.jar','--server.port=18081','--spring.datasource.url=jdbc:mariadb://localhost:3306/hobbyhub_verificacao?createDatabaseIfNotExist=true&connectionTimeZone=America/Sao_Paulo','--hobbyhub.midias.diretorio=./target/smoke-midias') -WindowStyle Hidden -PassThru -RedirectStandardOutput 'target/smoke-server.log' -RedirectStandardError 'target/smoke-server-error.log'
 for ($taskAttempt=0; $taskAttempt -lt 45; $taskAttempt++) {
  if ($script:taskServer.HasExited) { throw 'Servidor encerrou durante o início; consulte target/smoke-server.log.' }
  try { $taskResponse = Invoke-WebRequest 'http://localhost:18081/login' -TimeoutSec 2; if($taskResponse.StatusCode -eq 200) { return } } catch { }
  Start-Sleep -Seconds 1
 }
 throw 'Tempo de início excedido.'
}
function Login-HobbyHubSmoke {
 $taskLogin = Invoke-WebRequest 'http://localhost:18081/login' -SessionVariable taskSession
 $taskCsrf = [regex]::Match($taskLogin.Content, 'name="_csrf"[^>]*value="([^"]+)"').Groups[1].Value
 if (-not $taskCsrf) { throw 'Token CSRF do login não encontrado.' }
 $taskFeed = Invoke-WebRequest 'http://localhost:18081/login' -Method Post -WebSession $taskSession -Body @{email='demo@hobbyhub.local';password='HobbyHub123!';_csrf=$taskCsrf}
 if ($taskFeed.Content -notmatch 'Pequenas paix') { throw 'Login não abriu o feed.' }
 return $taskSession
}
try {
 Start-HobbyHubSmoke
 $taskSession = Login-HobbyHubSmoke
 $taskForm = Invoke-WebRequest 'http://localhost:18081/posts/novo' -WebSession $taskSession
 if ($taskForm.Content -notmatch 'Futebol') { throw 'Tags não carregadas no formulário.' }
 $taskToken = Invoke-RestMethod 'http://localhost:18081/api/csrf' -WebSession $taskSession
 $taskBody = @{conteudo='Verificação de persistência do HobbyHub';tagGeralId=1;tagEspecificaId=4} | ConvertTo-Json
 $taskPost = Invoke-RestMethod 'http://localhost:18081/api/posts' -Method Post -WebSession $taskSession -Headers @{'X-CSRF-TOKEN'=$taskToken.token} -ContentType 'application/json; charset=utf-8' -Body ([System.Text.Encoding]::UTF8.GetBytes($taskBody))
 if ($taskPost.autor -ne 'hobbylover' -or $taskPost.tags.Count -ne 2) { throw 'Post inválido.' }
 $taskSpec = Invoke-RestMethod 'http://localhost:18081/v3/api-docs' -WebSession $taskSession
 if (-not $taskSpec.paths.'/api/posts'.post) { throw 'Swagger não contém publicação.' }
 $taskPngBytes = [Convert]::FromBase64String('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aT1cAAAAASUVORK5CYII=')
 [IO.File]::WriteAllBytes((Join-Path (Get-Location) 'target/smoke-foto.png'), $taskPngBytes)
 $taskMidia = Invoke-RestMethod 'http://localhost:18081/api/posts' -Method Post -WebSession $taskSession -Headers @{'X-CSRF-TOKEN'=$taskToken.token} -Form @{conteudo='Foto de teste';tagGeralId='1';tagEspecificaId='4';arquivo=(Get-Item 'target/smoke-foto.png')}
 $taskImagem = Invoke-WebRequest ('http://localhost:18081' + $taskMidia.urlImagem) -WebSession $taskSession
 if ($taskImagem.Headers.'Content-Type' -notmatch 'image/png') { throw 'Imagem não foi servida como PNG.' }
 $taskGrande = [IO.File]::OpenWrite((Join-Path (Get-Location) 'target/smoke-grande.png'))
 $taskGrande.SetLength(26214401)
 $taskGrande.Dispose()
 $taskLimite = Invoke-WebRequest 'http://localhost:18081/api/posts' -Method Post -SkipHttpErrorCheck -WebSession $taskSession -Headers @{'X-CSRF-TOKEN'=$taskToken.token} -Form @{conteudo='Arquivo excessivo';tagGeralId='1';tagEspecificaId='4';arquivo=(Get-Item 'target/smoke-grande.png')}
 if ($taskLimite.StatusCode -ne 413 -or $taskLimite.Content -notmatch '25 MB') { throw ('Limite de upload falhou: ' + $taskLimite.StatusCode) }
 Stop-Process -Id $taskServer.Id
 $taskServer.WaitForExit()
 Start-HobbyHubSmoke
 $taskSession = Login-HobbyHubSmoke
 $taskPosts = Invoke-RestMethod 'http://localhost:18081/api/posts' -WebSession $taskSession
 if (-not ($taskPosts | Where-Object { $_.id -eq $taskPost.id -and $_.conteudo -eq 'Verificação de persistência do HobbyHub' })) { throw 'Post não persistiu após reiniciar.' }
 $taskImagem = Invoke-WebRequest ('http://localhost:18081' + $taskMidia.urlImagem) -WebSession $taskSession
 if ($taskImagem.RawContentStream.Length -ne $taskPngBytes.Length) { throw 'Mídia não persistiu após reiniciar.' }
 Write-Output 'SMOKE SUCCESS: login real, bootstrap de tags, formulário, publicação via API com CSRF, OpenAPI, upload multipart, erro 413 e persistência de post/mídia após reiniciar.'
} finally {
 if ($taskServer -and -not $taskServer.HasExited) { Stop-Process -Id $taskServer.Id; $taskServer.WaitForExit() }
}

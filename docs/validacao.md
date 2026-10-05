# Verificação executada — 01/10/2026

Ambiente: Windows 11, Java 21.0.12, Maven 3.9.11, Spring Boot 3.5.16.

- Maven package: BUILD SUCCESS.
- PostarIntegrationTest: 17 testes; zero falhas, zero erros e zero ignorados.
- JAR: target/hobbyhub-0.0.1-SNAPSHOT.jar.
- Verificação HTTP do JAR: SMOKE SUCCESS.

Cobertura dos testes: autoria pela sessão, conteúdo, tipos e hierarquia das tags, autenticação, CSRF, renderização Thymeleaf, resposta API sem senha, OpenAPI, upload de imagem e entrega dos mesmos bytes, vídeo com resposta parcial HTTP Range, rejeição de arquivo disfarçado, proteção do diretório de armazenamento e remoção de mídia ao cancelar a transação.

A verificação HTTP fez login real, carregou formulário e tags, publicou por JSON e multipart com CSRF, consultou OpenAPI, recebeu a imagem armazenada, rejeitou um arquivo acima de 25 MB com HTTP 413 e recuperou post e mídia após encerrar e iniciar novamente o processo. O banco antigo de demonstração foi atualizado com os pais das tags no início.

Usou porta 18081, banco target/smoke-hobbyhub e mídia target/smoke-midias. O banco padrão data não recebeu essas postagens de teste. Processos de verificação foram encerrados.

Para reproduzir no PowerShell 7:

```powershell
mvn package
pwsh -File scripts/verificar-execucao.ps1
```

O teste de vídeo usa cabeçalho sintético e verifica upload, player HTML e requisição parcial, não reprodução real. Não houve inspeção visual das páginas nem teste de carga. Verificação de arquivos é por extensão/assinatura, não decodificação completa ou antivírus.

Os diagramas de classes e sequência foram aceitos pelo parser Mermaid 11.12.0. O SVG foi renderizado em PNG e inspecionado visualmente; essa verificação de sintaxe/renderização não equivale a validação formal do metamodelo UML.

## Transição para MariaDB do XAMPP — 02/10/2026

- MariaDB 10.4.32 em localhost:3306, schema hobbyhub. Driver mariadb-java-client 3.5.8 gerenciado pelo Spring Boot.
- 17 testes de integração: zero falhas e zero erros, em schema exclusivo removido ao terminar.
- Teste HTTP em schema separado: login, formulário, publicação com CSRF, OpenAPI, upload multipart, HTTP 413 e persistência de post/mídia após reiniciar.
- Banco real: login da conta existente e recuperação do post migrado com duas tags confirmados via HTTP.
- Transferidos 1 usuário, 6 tags, 1 post e 2 vínculos post/tag. Arquivo H2 preservado em data/backups/hobbyhub-h2-2026-10-02.mv.db.
- JAR conferido: contém o driver MariaDB e não contém H2 nem o driver MySQL anterior.

Esta validação substitui a configuração de banco das verificações anteriores; a aplicação e os testes agora usam o servidor externo.

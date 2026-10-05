# Banco de dados do HobbyHub

A aplicação usa o MariaDB 10.4.32 fornecido pelo módulo chamado MySQL no XAMPP. O servidor foi verificado em localhost:3306, com usuário root e senha vazia. O schema hobbyhub foi criado nessa instância.

## Configuração

O pom.xml contém org.mariadb.jdbc:mariadb-java-client, com versão gerenciada pelo Spring Boot. O driver oficial identifica corretamente a versão do servidor: https://mariadb.com/docs/connectors/mariadb-connector-j/installing-mariadb-connectorj

A URL padrão é jdbc:mariadb://localhost:3306/hobbyhub?connectionTimeZone=America/Sao_Paulo. MYSQL_URL, MYSQL_USER e MYSQL_PASSWORD permitem substituir a conexão. Hibernate usa ddl-auto=update; não apaga os dados a cada início.

As tabelas são usuarios, tags, posts, usuario_tags e post_tags. Imagens e vídeos continuam em data/midias; o banco guarda apenas suas referências.

## Executar

Inicie o módulo MySQL no XAMPP. Apache não é necessário. Na pasta do projeto:

~~~powershell
mvn spring-boot:run
~~~

Acesse http://localhost:8080. Workbench é um cliente opcional para consultar o servidor; não inicia a aplicação.

## Testes

Execute mvn test com o servidor funcionando. Cada execução cria um schema hobbyhub_test_<UUID> exclusivo e o remove ao terminar. Os testes não usam nem apagam as tabelas do schema hobbyhub. MYSQL_TEST_SERVER_URL pode indicar outro servidor, por exemplo jdbc:mariadb://localhost:3307/, incluindo a barra final. O usuário precisa de permissão para criar e remover schemas de teste.

O script scripts/verificar-execucao.ps1 usa o schema separado hobbyhub_verificacao, a porta HTTP 18081 e mídia em target/smoke-midias. Esse schema permanece para conferir a persistência.

## Migração do H2 em 02/10/2026

Foram transferidos e conferidos 1 usuário, 6 tags, 1 post e 2 vínculos post/tag. Não havia vínculos usuario/tag. A transferência preservou ids, senhas, datas e referências de mídia. O destino estava vazio e a transferência foi executada em uma transação.

O H2 foi removido da configuração e das dependências da aplicação e dos testes. O arquivo antigo foi preservado em data/backups/hobbyhub-h2-2026-10-02.mv.db. A ferramenta pontual de migração ficou em target/mysql e não é necessária para rodar o projeto.

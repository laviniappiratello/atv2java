# Automanager — Atividade 2

Microserviço Java (Spring Boot) para cadastro de clientes. Usa Maven, H2 (runtime) e requer Java 17.

## Pré-requisitos
- Java 17 (JDK)
- Git
- (Opcional) Maven — o projeto inclui o Maven Wrapper (`mvnw` / `mvnw.cmd`)

## Como executar (Windows)
1. Abra o `cmd.exe` e entre na pasta do projeto:
```cmd
cd automanager
```
2. Compilar:
```cmd
mvnw.cmd clean package
```
3. Executar em modo de desenvolvimento:
```cmd
mvnw.cmd spring-boot:run
```

## Configuração
- Arquivo de configuração: `automanager/src/main/resources/application.properties`
- Porta padrão: `8080` (alterar `server.port` ou usar a variável de ambiente `SERVER_PORT`)

# RIP - Rest in peace

Gerador de serviços mockados estupidamente simples

## Objetivos

1. Gerar um serviço rest mockado sem preocupações de infraestrutura
1. Serviços simples, sem qualquer inteligência
1. Possibilidade de respostas distintas de acordo com a presença de conteúdo na requisição
1. Respondendo os verbos mais comuns (GET, POST, PUT, DELETE), outros podem ser implementados se necessário

O objetivo principal é criar um endpoint rest local que responda sempre o 
mesmo conteúdo dado que o corpo da mensagem contenha determinada string.

## Utilização

O servidor e os serviços são criados programaticamente, para serem incluídos em um
teste unitário, uma aplicação web ou um servidor remoto. O servidor responde em 
localhost, em uma porta definida.

#### Exemplos
Cria o servidor na porta padrão (7777) respondendo à `GET /test` e, se
*xpto* estiver no body da requisição, responde *Hello World*:

```java
localhost().get("/test").contains("xpto").respond("Hello World");
```

Cria o servidor na porta 8888 respondendo à `POST /test` e, se *xpto* e *1234* estiverem
no body, responde com o conteúdo do arquivo `/test/hello.json` 

```java
localhost(8888).post("/test").contains("xpto").and().contains("1234").respond(withFile("/test/hello.json"));
```

Responde à `POST /test` de acordo com o conteúdo do body, caindo no caso default (último)
se não encontrar nenhuma correspondência

```java
localhost().post("/test").contains("test1").respond(withFile("/test/1.json"));
localhost().post("/test").contains("test2").respond(withFile("/test/2.json"));
localhost().post("/test").respond(withFile("File not found"));
```

## Como criar os serviços mockados em uma aplicação dentro de um servlet container qualquer (Ex. tomcat, liberty, jboss)

Para testes unitários basta a chamada aos métodos de criação de rotas, como acima,
preferencialmente em um método `@BeforeClass`. Se for necessário executar
testes em uma aplicação web publicada, os serviços podem ser iniciados na mesma
aplicação com a utilização de um filtro.

A sintaxe de criação de rotas não é alterada, mas naturalmente a porta não deve ser
utilizada.

### Acrescentar a dependencia no pom

```xml
        <!-- Mocks -->
        <dependency>
            <groupId>net.technearts</groupId>
            <artifactId>rip</artifactId>
            <version>0.0.3</version>
        </dependency>
```

### Criar uma classe extendendo RipWebApp

```java

package rest.mock;

import static net.technearts.rip.RipServer.localhost;
import static net.technearts.rip.RipServer.withFile;

import net.technearts.rip.RipWebApp;

public class RestMock extends RipWebApp {

    @Override
    public void setup() {
        localhost().post("/test").contains("0123456789")
                .respond(withFile("/files/test1.json"));
        localhost().post("/test")
                .respond(withFile("/files/test2.json"));
    }
}
```

### Acrescentar o filtro no web.xml

```xml
    <filter>
        <filter-name>RipFilter</filter-name>
        <filter-class>net.technearts.rip.RipWebFilter</filter-class>
        <init-param>
            <param-name>applicationClass</param-name>
            <param-value>rest.mock.RestMock</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>RipFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
```

Obs.: o serviço irá responder na mesma porta e contexto da aplicação!

### Acrescentar os arquivos com as respostas ao path. No maven, sob /src/main/resources, por exemplo:
- /files/test1.json
- /files/test2.json

## Construção

Obs.: Para abrir no eclipse é preciso instalar o (lombok)|[https://projectlombok.org]

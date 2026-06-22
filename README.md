# Banco Digital API

API REST simulando operações básicas de um banco digital.

**Desenvolvido por:** Ana Clara de Oliveira Anderle

---

# Tecnologias Utilizadas

* Java 25
* Spring Boot 4.0.7
* PostgreSQL
* Flyway
* Apache Kafka
* Lombok
* Maven
* Swagger / OpenAPI
* Prometheus
* Grafana
* JUnit 5
* Mockito
* Testcontainers

---

# Funcionalidades

* Cadastro de clientes
* Cadastro de contas bancárias
* Importação em massa via CSV
* Importação em massa via TXT
* Consulta de saldo
* Transferência entre contas
* Histórico financeiro (extrato)
* Notificações assíncronas via Kafka
* Idempotência de requisições
* Observabilidade com Correlation ID
* Métricas via Prometheus
* Dashboards via Grafana

---

# Executando o Projeto

## Pré-requisitos

* Java 25
* Maven 3.9+
* Docker

## Subindo a infraestrutura

```bash
docker-compose up -d
```

Serão iniciados os serviços necessários para desenvolvimento:

* PostgreSQL
* Kafka
* Prometheus
* Grafana

## Executando a aplicação

```bash
./mvnw spring-boot:run
```

ou

```bash
mvn spring-boot:run
```

## Executando os testes

```bash
mvn clean test
```

---

# Documentação da API

Após iniciar a aplicação, a documentação Swagger estará disponível em:

```text
http://localhost:8080/swagger-ui.html
```

ou

```text
http://localhost:8080/swagger-ui/index.html
```

Através do Swagger é possível visualizar todos os endpoints disponíveis, exemplos de requisições, respostas e códigos de erro.

---

# Decisões Técnicas

## Java 25

Foi utilizada a versão LTS mais recente do Java, garantindo acesso aos recursos mais atuais da plataforma, melhorias de desempenho e suporte de longo prazo.

## Spring Boot 4.0.7

Foi escolhida a versão mais recente do Spring Boot compatível com as dependências utilizadas no projeto, proporcionando acesso às funcionalidades mais atuais do ecossistema Spring.

## PostgreSQL

Banco de dados amplamente consolidado no mercado, reconhecido pela confiabilidade, desempenho e excelente suporte a operações transacionais.

## Flyway

Utilizado para versionamento e gerenciamento das migrations do banco de dados, permitindo rastreabilidade e reprodutibilidade da estrutura da aplicação em diferentes ambientes.

## Apache Kafka

Escolhido como mecanismo de mensageria devido à sua robustez, escalabilidade e ampla utilização em sistemas distribuídos de alta demanda.

## Lombok

Utilizado de forma criteriosa para reduzir código repetitivo, principalmente em getters, setters e construtores. Seu uso foi limitado aos casos em que trouxe ganhos reais de legibilidade sem comprometer a clareza da implementação.

## Prometheus e Grafana

Ferramentas open source amplamente utilizadas para observabilidade. O Prometheus realiza a coleta das métricas da aplicação, enquanto o Grafana permite a visualização e análise dessas informações através de dashboards.

## Maven

Ferramenta consolidada no ecossistema Java, utilizada para gerenciamento de dependências, build da aplicação e execução dos testes.

---

# Arquitetura

## Arquitetura em Camadas

Foi adotada uma arquitetura em camadas por ser uma abordagem simples, amplamente conhecida e adequada para demonstrar os conceitos exigidos.

A separação entre controllers, services, repositories, mappers e DTOs permite melhor organização do código, manutenção facilitada e menor acoplamento entre responsabilidades.

---

# Modelagem de Dados

As entidades foram mantidas enxutas, contendo apenas as informações necessárias para testes.

## Cliente

Responsável pelo cadastro dos dados básicos do titular.

## Conta

Representa a conta bancária associada a um cliente e mantém o saldo atual.

## Transação

Responsável por registrar a transferência entre contas de origem e destino, garantindo rastreabilidade das movimentações financeiras.

## Histórico

Responsável por armazenar os lançamentos de débito e crédito de cada conta, permitindo a futura geração de extratos e consultas detalhadas.

## Índices

Foram criados índices nos campos mais frequentemente utilizados em consultas e relacionamentos, visando otimizar o desempenho e preparar a aplicação para cenários com maior volume de dados.

---

# Importação em Massa

Foi implementada importação em lote através de arquivos CSV e TXT.

A escolha foi baseada em cenários comuns do mercado financeiro, onde planilhas ainda são amplamente utilizadas para carga e migração de dados. O suporte adicional a arquivos TXT simplifica a execução de testes e validações rápidas.

---

# Transferências Financeiras

As transferências foram implementadas de forma síncrona para fornecer retorno imediato ao cliente.

A atomicidade da operação é garantida através do uso de transações utilizando `@Transactional`, assegurando que todas as etapas sejam concluídas com sucesso ou revertidas integralmente em caso de falha.

Para cenários concorrentes foi adotado `PESSIMISTIC_WRITE`, evitando problemas como perda de atualização e inconsistência de saldo.

Em cenários de altíssimo volume de transações, uma abordagem baseada em mensageria poderia ser considerada para aumentar a escalabilidade horizontal do processamento. Entretanto, para esse escopo, a abordagem síncrona oferece maior simplicidade e previsibilidade.

---

# Idempotência

Os endpoints de escrita utilizam chave de idempotência para evitar processamento duplicado de requisições.

Essa estratégia reduz riscos de duplicidade causados por reenvios de requisições, falhas de rede ou tentativas repetidas do cliente.

---

# Mensageria

Após a conclusão bem-sucedida de uma transferência, uma notificação é publicada em um tópico Kafka.

Essa abordagem permite que o processamento das notificações ocorra de forma assíncrona, evitando impacto direto no tempo de resposta da operação principal.

Não foi implementado um consumidor específico neste projeto, considerando que o processamento da notificação poderia ser realizado futuramente por outro microsserviço especializado ou por outro módulo da própria aplicação, cenário comum em arquiteturas orientadas a eventos.

---

# Observabilidade

Foi adotado o uso de Correlation ID para rastreamento completo das operações.

O identificador é propagado através:

* Dos logs da aplicação.
* Das respostas de erro.
* Das exceções lançadas.
* Dos eventos enviados ao Kafka.

Essa estratégia facilita significativamente a investigação de problemas, correlação de eventos e análise de fluxos distribuídos.

---

# Mapeamento de Objetos

Foi optado por não utilizar ferramentas de geração automática de mapeamento, como MapStruct.

Os mapeamentos foram implementados manualmente através de classes Mapper específicas, mantendo total controle sobre as conversões e evitando aumento no tempo de compilação do projeto.

---

# Estratégia de Testes

Foram implementados diferentes níveis de testes para aumentar a confiabilidade da aplicação.

## Testes Unitários

Validação isolada das regras de negócio e componentes da camada de serviço.

## Testes de Integração

Execução utilizando PostgreSQL e Kafka reais através do Testcontainers, garantindo maior proximidade com o ambiente de execução.

## Testes de Concorrência

Validação do comportamento das transferências sob múltiplas execuções simultâneas, garantindo consistência dos saldos.

---

# Docker Compose

Foi disponibilizado um arquivo `docker-compose.yml` para facilitar a configuração do ambiente de desenvolvimento.

Com apenas um comando é possível iniciar toda a infraestrutura necessária para execução e testes da aplicação, reduzindo o esforço de setup e garantindo maior padronização entre ambientes.

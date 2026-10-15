# Sistema Distribuído de Processamento de Mensagens com RabbitMQ, Java e IA

Este projeto implementa uma arquitetura de microsserviços orientada a eventos utilizando **Java**, **RabbitMQ**, **Maven** e **Docker**. O sistema simula um cenário real de backend onde um componente produtor gera fluxo contínuo de dados (avaliações de clientes e pedidos de suporte) e microsserviços consumidores processam esses dados em paralelo, aplicando lógica de Inteligência Artificial para classificação e categorização automatizada em tempo real.

---

##  Arquitetura do Sistema

A solução está dividida em componentes independentes isolados em contentores Docker, orquestrados através do `docker-compose`:

1. **Produtor (`generator`):**
   - Responsável por simular a entrada de dados do sistema.
   - Publica mensagens de forma contínua numa Exchange do RabbitMQ utilizando diferentes *Routing Keys* (`review.product` e `support.request`).

2. **Consumidor de Avaliações (`consumer-reviews`):**
   - Escuta a fila dedicada às avaliações de produtos.
   - Processa cada texto recebido e aplica uma camada de IA/análise para classificar o sentimento do cliente.

3. **Consumidor de Suporte (`consumer-support`):**
   - Escuta a fila dedicada aos chamados técnicos.
   - Analisa o conteúdo das mensagens e categoriza o tipo de ocorrência de suporte automaticamente.

---

##  Descrição das Inteligências Artificiais (IAs)

O sistema emprega lógica de processamento inteligente em dois fluxos distintos de consumo:

* **IA 1 - Classificação de Sentimento (`consumer-reviews`):**
  - **Objetivo:** Analisar criticamente o texto gerado nas avaliações dos clientes.
  - **Funcionamento:** O modelo processa a linguagem natural da mensagem e categoriza o feedback em três estados distintos: `POSITIVO`, `NEUTRO` ou `NEGATIVO`. Isto permite que empresas automatizem a triagem de satisfação dos utilizadores em grande escala.

* **IA 2 - Categorização de Suporte (`consumer-support`):**
  - **Objetivo:** Identificar e agrupar automaticamente os chamados técnicos recebidos na fila de atendimento.
  - **Funcionamento:** O motor de IA lê o enunciado do problema reportado pelo utilizador e atribui uma categoria operacional específica (ex: *Problema de conexão*, *Problema de acesso*, *Problema de desempenho*, etc.), agilizando o direcionamento interno para as equipas responsáveis.

---

##  Instruções de Execução

Certifique-se de que tem o **Docker** e o **Docker Compose** instalados e em execução no seu computador.

1. Abra o terminal na pasta raiz do projeto (onde se encontra o ficheiro `docker-compose.yml`).
2. Execute o comando para construir as imagens do zero e iniciar todos os microsserviços em segundo plano:
   ```bash
   docker-compose up --build -d
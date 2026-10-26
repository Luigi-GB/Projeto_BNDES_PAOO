# BNDES_PAOO

API em **Java 21 + Spring Boot 4** (JPA + PostgreSQL) com uma interface em **HTML, CSS e JavaScript** para importar o CSV de
*operações de financiamento (não automáticas)* do BNDES. O usuário clica em um botão, escolhe o arquivo e o sistema lê, limpa, valida e mostra um relatório com linhas lidas, importadas, rejeitadas e os motivos.

## Dados

Foi utilizado o link abaixo como exemplo para a construção dessa aplicação:
https://dadosabertos.bndes.gov.br/pt_BR/dataset/operacoes-financiamento

Foram considerados apenas os dados das operações não automáticas, pois são voltadas para projetos mais complexos e de grande porte, ou que envolvem volumes expressivos de recursos, fazendo com que tenha mais importância a criação de um sistema para análise desses dados, dado que são projetos mais robustos e com maior impacto na sociedade brasileira.

## Como rodar

Requisitos: JDK 21 e um arquivo `.env` na pasta `BNDES/` com as variáveis do banco:

```
DB_URL=jdbc:postgresql://...
DB_USERNAME=...
DB_PASSWORD=...
```

```bash
./mvnw spring-boot:run      # Windows: mvnw.cmd spring-boot:run
```

Abra http://localhost:8080 e clique em **Selecionar arquivo CSV**.

## Endpoints

| Rota | Controller | O que faz |
|---|---|---|
| `POST /api/importacao` | `ImportacaoController` | Recebe o CSV (campo `arquivo`) e devolve o relatório de importação |
| `GET /api/painel/tecnologia?criterio=` | `PainelController` | Dados agregados para os gráficos de tecnologia (`COMBINADO`, `INOVACAO` ou `PALAVRA_CHAVE`) |
| `/api/operacoes` | `ContratoController` | CRUD de contratos |
| `/api/clientes` | `ClienteController` | CRUD de clientes |

## Estrutura

```
src/main/java/br/com/fatec/api_bndes/
├── BndesPaooApplication.java
├── controller/   ContratoController, ClienteController, ImportacaoController, TratadorDeErros (só da importação)
├── service/      ContratoService, ClienteService, ImportacaoService (fluxo geral), ConversorOperacao (regras por campo)
├── repository/   ContratoRepository, ClienteRepository
├── model/        Contrato, Cliente (entidades JPA)
├── dto/          FiltroContrato, AnaliseContrato, OperacaoFinanciamento (linha limpa do CSV), RelatorioImportacao (resposta)
├── util/         LinhaCsvParser (divide a linha), Normalizador (texto, dinheiro, datas, CNPJ)
└── exception/    LinhaInvalidaException, MotivoRejeicao, ArquivoInvalidoException
src/main/resources/
├── application.properties
└── static/       index.html (importação), painel.html (gráficos), css/, js/bndes.js, js/painel.js
```

As colunas `municipio_codigo` e `subsetor_cnae_codigo` são **desconsideradas** (não são lidas, validadas, gravadas nem entram na duplicidade), pois não trazem uma informação relevante que podemos usar, se tratam apenas de códigos específicos. Todas as outras 32 são mantidas.

## O que é feito com cada linha

| Etapa | Regra |
|---|---|
| Codificação | UTF-8 se for válido; senão Windows-1252 (caso do arquivo do BNDES). BOM é removido. |
| Separador | Detectado no cabeçalho (`;`, `,` ou tab). Colunas são localizadas pelo **nome**, não pela posição. |
| Texto | Remove espaços das pontas e repetidos, maiúsculas, remove acentos (configurável), troca `–` por `-`. |
| Vazios | `""`, `-` e `----------` viram `null`. `SEM MUNICÍPIO` também. |
| Dinheiro | `9090000,0`, `1.234,56`, `1234.56`, `R$ 10,5` → `BigDecimal` (sem `double`), 2 casas, sem negativos. Formato ambíguo (`1,234,567`) é rejeitado. |
| Datas | `yyyy-MM-dd` ou `dd/MM/yyyy` → `LocalDate`. Datas impossíveis, futuras ou antes de 1950 são rejeitadas. |
| Números | Prazos → `Integer` ≥ 0; contrato → `Long`; inovação SIM/NÃO → `Boolean`. |
| CNPJ | Precisa ter 14 dígitos (e não ser todo igual); é gravado formatado. Dígitos verificadores errados geram só um **aviso**. |
| UF | Uma das 27 UFs ou `IE` (valor que existe na própria base). |
| Obrigatórios | `cliente`, `cnpj`, `numero_do_contrato`, `data_da_contratacao`, `valor_contratado_reais`. |
| Duplicidade | Linha idêntica a outra (todos os campos, após a limpeza) é rejeitada, apontando a linha original. O número do contrato **não** é chave: ele se repete legitimamente. |

Cada linha rejeitada é contada uma vez, pelo primeiro problema encontrado. O relatório mostra a contagem por motivo
com até 5 exemplos (número da linha + detalhe).

## Limitações conhecidas

- O arquivo é carregado inteiro na memória (adequado para ~20 MB).

## Painel de gráficos (financiamentos de tecnologia)

Depois de importar o CSV, o botão **Ver gráficos de financiamentos de tecnologia** abre `painel.html`
(ou acesse http://localhost:8080/painel.html). Os gráficos são desenhados em SVG/HTML puro, sem biblioteca externa.

- **Classificação** (`ClassificadorTecnologia`): contrato de tecnologia = coluna `inovacao = SIM` **e/ou** palavra-chave na
  `descricao_do_projeto`. A lista de palavras é uma constante (`PALAVRAS_CHAVE`) e é enxuta de propósito: termos como
  INFORMATICA, AUTOMACAO e DIGITAL pegavam linhas de crédito para máquinas, TV digital e obras elétricas.
- **Agregação** (`PainelTecnologiaService`): recebe uma `List<OperacaoFinanciamento>` e não depende de onde ela veio.
- **Fonte dos dados**: `OperacoesImportadas` guarda em memória as linhas válidas da última importação. Quando a gravação no
  banco existir, basta passar ao `PainelTecnologiaService.montar(...)` a lista lida do repositório.
- Os dados do painel somem ao reiniciar a aplicação (é preciso importar de novo).

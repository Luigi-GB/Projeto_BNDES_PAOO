package br.com.fatec.api_bndes.exception;

public enum MotivoRejeicao {
    COLUNAS_DIFERENTES("Quantidade de colunas diferente do cabeçalho"),
    ASPAS_INVALIDAS("Aspas não fechadas ou linha mal formatada"),
    LINHA_EM_BRANCO("Linha em branco"),
    TEXTO_CORROMPIDO("Texto corrompido (caracteres inválidos)"),
    CAMPO_OBRIGATORIO_VAZIO("Campo obrigatório vazio"),
    CNPJ_INVALIDO("CNPJ em formato inválido"),
    DATA_INVALIDA("Data inválida"),
    VALOR_MONETARIO_INVALIDO("Valor monetário inválido"),
    NUMERO_INVALIDO("Número inválido"),
    UF_INVALIDA("UF inválida"),
    VALOR_INVALIDO("Valor fora do formato esperado"),
    LINHA_DUPLICADA("Linha duplicada");

    private final String descricao;

    MotivoRejeicao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}

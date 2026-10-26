package br.com.fatec.api_bndes.exception;

/** Lançada quando UMA linha do CSV não pode ser importada. A linha é ignorada e o motivo é registrado. */
public class LinhaInvalidaException extends RuntimeException {

    private final MotivoRejeicao motivo;
    private final String coluna;

    public LinhaInvalidaException(MotivoRejeicao motivo, String coluna, String detalhe) {
        super(detalhe);
        this.motivo = motivo;
        this.coluna = coluna;
    }

    public MotivoRejeicao getMotivo() {
        return motivo;
    }

    public String getColuna() {
        return coluna;
    }

    /** Texto usado para agrupar rejeições iguais no relatório. */
    public String chaveDeAgrupamento() {
        return coluna == null ? motivo.getDescricao() : motivo.getDescricao() + " [" + coluna + "]";
    }
}

/**
 * 
 */
package com.cmm.jft.data.extractor.marketdata;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;

import com.cmm.jft.core.format.DateTimeFormatter;
import com.cmm.jft.core.format.DoubleFormatter;
import com.cmm.jft.core.format.FormatterFactory;
import com.cmm.jft.core.format.FormatterTypes;
import com.cmm.jft.core.format.IntFormatter;
import com.cmm.jft.vo.Extractable;
import com.cmm.jft.vo.OrderEventVO;
import com.cmm.jft.data.files.CSV;
import com.cmm.logging.Logging;

/**
 * <p><code>BovespaOfferFileExtractor.java</code></p>
 * @author Cristiano M Martins
 * @version 06/03/2015 16:17:16
 *
 */
public class BovespaOfferFileExtractor extends BovespaFileExtractor {

	public static void main(String[] args) {
		
		BovespaOfferFileExtractor bfe = new BovespaOfferFileExtractor();
		bfe.fileName = "D:\\MarketData\\BMF\\WINM15.csv";
		System.out.println(bfe.extract().size());
		
	}
	
	
	/* (non-Javadoc)
	 * @see com.cmm.jft.data.extractor.Extractor#extract()
	 */
	@Override
	public List<Extractable> extract() {
		
		List<Extractable> bsEvents = new ArrayList<>(1000000);
		try {
			DateTimeFormatter dtf = (DateTimeFormatter) FormatterFactory.getFormatter(FormatterTypes.DATE_TIME_F8);
			DateTimeFormatter tf = (DateTimeFormatter) FormatterFactory.getFormatter(FormatterTypes.TIME_F3);
			DoubleFormatter df = (DoubleFormatter) FormatterFactory.getFormatter(FormatterTypes.DOUBLE);
			IntFormatter intf = (IntFormatter) FormatterFactory.getFormatter(FormatterTypes.INT);
			
			CSV csv = new CSV(fileName, ";", "RT", "RH");
			while (csv.hasNext()) {
				String[] vs = csv.readLine();

				if (vs != null && vs[0] != null) {
					
					OrderEventVO eventVO = new OrderEventVO();
					eventVO.sessionDate = dtf.parse(vs[0]);
					
					// layout dos arquivos foi alterado devido a mudanca para o
					// sistema de negociacao Puma
					if (vs.length == 12) {// /....!fk
//						-----------------------------------------------------------
//						Coluna                 Posicao Inicial  Tamanho   Descricao
//						-----------------------------------------------------------
//						Data Sessao                          1       10   Data da Sessao
//						Papel                               50       12   Codigo do Instrumento
//						Sequencia                           63       10   Numero de Sequencia da Oferta
//						Preco Of.Compra                     74       20   Preco da Oferta
//						Qtd.Total Of.Compra                 95       18   Quantidade Total
//						Qtd.Negociada Of.Compra            114       18   Quantidade Negociada
//						Hora Prioridade                    133       15   Hora de registro da oferta no sistema (com a precisao Tandem, no formato, HH:MM:SS.NNNNNN), utilizada como indicadora de prioridade
//						Data de Entrada Of.Compra          149       19   Data/Hora de Entrada da Oferta
//						Estado Of.Compra                   169        1   Indicador de estado da ordem " " - aceite "E" - eliminada (EOC) "G" - congelada "O" - cancelada seguido de uma acao no instrumento (por ex- Papel Reservado) "X" - totalmente executada "M" - modificada "D" - disparada "A" - anulada (corretora) "R" - rejeitada pelo Surveillance, seguido de um congelamento. Apos 04/03/2013 devido a migracao para o PUMA alguns ativos estarao valorizados com: 0 - Novo / 1 - Negociada parcialmente / 2 - Totalmente executada / 4 - Cancelada / 5 - Modificada / 8  - Rejeitada / C - Expirada
//						Data Modif. Of.Compra              171       10   Data de Modificacao da Oferta
//						Nr.Of.Compra Modif.                182       10   Numero da Oferta Modificada
//						Hora Fim Tratam. Of.Compra         193       19   Hora de Fim de Tratamento (contem Hora da Anulacao quando Indicador de Estado da Orderm for igual a "A")
						
						eventVO.orderID = vs[2];
						eventVO.price = df.parse(vs[3]);
						eventVO.volume = df.parse(vs[4]);
						eventVO.tradedVolume = df.parse(vs[5]);
						eventVO.orderTime = tf.parse(vs[6]);
						eventVO.orderDate = dtf.parse(vs[7]);
						eventVO.orderStatus = vs[8];

					} else if (vs.length >= 14 && vs.length <16) {
//						-----------------------------------------------------------
//						Coluna                 Posicao Inicial  Tamanho   Descricao
//						-----------------------------------------------------------
//						Data Sessao                          1       10   Data da Sessao
//						Simbolo do Instrumento              12       50   Simbolo do Instrumento
//						Sentido Of.Compra                   63        1   Indicador de sentido da ordem: "1" - compra / "2" - venda
//						Sequencia                           65       15   Numero de Sequencia da Oferta
//						GenerationID - Of.Compra            81       15   Numero de geracao (GenerationID) da Oferta de Compra. Quando um negocio for gerado por 2 ofertas com quantidade escondida e isso gerar "n" linhas será gravado aqui a maior geracao.
//						Cod do Evento da Of.Compra          97        3   Codigo do Evento da Ordem: 1 - New / 2 - Update / 3 - Cancel - Solicitado pelo participante / 4 - Trade / 5 - Reentry - Processo interno (quantidade escondida) / 6 - New Stop Price / 7 - Reject / 8 - Remove - Removida pelo Sistema (final de dia ou quando e totalmente fechada) / 9 - Stop Price Triggered / 11 - Expire - Oferta com validade expirada.
//						Hora Prioridade                    101       15   Hora de registro da oferta no sistema (no formato, HH:MM:SS.NNN), utilizada como indicadora de prioridade
//						Ind de Prioridade Of.Compra        117       10   Indicador de Prioridade. Alem do preco e a ordem para aparecer no Order Book.
//						Preco Of.Compra                    128       20   Preco da Oferta
//						Qtd.Total Of.Compra                149       18   Quantidade Total da Oferta. Se tiver alteracao ela reflete a nova quantidade.
//						Qtd.Negociada Of.Compra            168       18   Quantidade Negociada
//						Data Oferta Compra                 187       10   Data de Inclusao da Oferta. Pode ser uma data anterior a Data da Sessao, quando se tratar de uma Oferta com Validade.
//						Data de Entrada Of.Compra          198       19   Data/Hora de Entrada da Oferta (formato: DD/MM/AAAA HH:MM:SS)
//						Estado Of.Compra                   218        1   Indicador de estado da ordem: 0 - Novo / 1 - Negociada parcialmente / 2 - Totalmente executada / 4 - Cancelada / 5 - Modificada / 8  - Rejeitada / C - Expirada
//						Condicao Oferta                    220        1   Codigo que identifica a condicao da oferta. Pode ser: 0 - Oferta Neutra - e aquela que entra no mercado e nao fecha com oferta existente. / 1 - Oferta Agressora - e aquela que ingressa no mercado para fechar com uma oferta existente. / 2 - Oferta Agredida - e a oferta (existente) que e fechada com uma oferta agressora.
						
						eventVO.securityID = vs[1];
						eventVO.orderID = vs[3];
						eventVO.eventID = vs[4];
						eventVO.orderEvent = vs[5];
						eventVO.orderTime = tf.parse(vs[6]);
						eventVO.price = df.parse(vs[8]);
						eventVO.volume = df.parse(vs[9]);
						eventVO.tradedVolume = df.parse(vs[10]);
						eventVO.orderDate =dtf.parse(vs[12]);
						eventVO.orderStatus = vs[13];
						
					} else if (vs.length >= 16) {
// 						-----------------------------------------------------------
// 						Coluna Posicao Inicial Tamanho Descricao
// 						-----------------------------------------------------------
//						Data Sessao                          1       10   Data da Sessao
//						Simbolo do Instrumento              12       50   Simbolo do Instrumento
//						Sentido Of.Compra                   63        1   Indicador de sentido da ordem: "1" - compra / "2" - venda
//						Sequencia                           65       15   Numero de Sequencia da Oferta
//						GenerationID - Of.Compra            81       15   Numero de geracao (GenerationID) da Oferta de Compra. Quando um negocio for gerado por 2 ofertas com quantidade escondida e isso gerar "n" linhas será gravado aqui a maior geracao.
//						Cod do Evento da Of.Compra          97        3   Codigo do Evento da Ordem: 1 - New / 2 - Update / 3 - Cancel - Solicitado pelo participante / 4 - Trade / 5 - Reentry - Processo interno (quantidade escondida) / 6 - New Stop Price / 7 - Reject / 8 - Remove - Removida pelo Sistema (final de dia ou quando e totalmente fechada) / 9 - Stop Price Triggered / 11 - Expire - Oferta com validade expirada.
//						Hora Prioridade                    101       15   Hora de registro da oferta no sistema (no formato, HH:MM:SS.NNN), utilizada como indicadora de prioridade
//						Ind de Prioridade Of.Compra        117       10   Indicador de Prioridade. Alem do preco e a ordem para aparecer no Order Book.
//						Preco Of.Compra                    128       20   Preco da Oferta
//						Qtd.Total Of.Compra                149       18   Quantidade Total da Oferta. Se tiver alteracao ela reflete a nova quantidade.
//						Qtd.Negociada Of.Compra            168       18   Quantidade Negociada
//						Data Oferta Compra                 187       10   Data de Inclusao da Oferta. Pode ser uma data anterior a Data da Sessao, quando se tratar de uma Oferta com Validade.
//						Data de Entrada Of.Compra          198       19   Data/Hora de Entrada da Oferta (formato: DD/MM/AAAA HH:MM:SS)
//						Estado Of.Compra                   218        1   Indicador de estado da ordem: 0 - Novo / 1 - Negociada parcialmente / 2 - Totalmente executada / 4 - Cancelada / 5 - Modificada / 8  - Rejeitada / C - Expirada
//						Condicao Oferta                    220        1   Codigo que identifica a condicao da oferta. Pode ser: 0 - Oferta Neutra - e aquela que entra no mercado e nao fecha com oferta existente. / 1 - Oferta Agressora - e aquela que ingressa no mercado para fechar com uma oferta existente. / 2 - Oferta Agredida - e a oferta (existente) que e fechada com uma oferta agressora.
//						Corretora                          222        8   Codigo que identifica univocamente a corretora - Disponivel a partir de 03/2014
						
						eventVO.securityID = vs[1];
						eventVO.orderID = vs[3];
						eventVO.eventID = vs[4];
						eventVO.orderEvent = vs[5];
						eventVO.orderTime = tf.parse(vs[6]);
						eventVO.price = df.parse(vs[8]);
						eventVO.volume = df.parse(vs[9]);
						eventVO.tradedVolume = df.parse(vs[10]);
						eventVO.orderDate = dtf.parse(vs[12]);
						eventVO.orderStatus = vs[13];
						eventVO.orderCondition = vs[14];
						eventVO.broker = intf.parse(vs[15]);
						
					}
					
					bsEvents.add(eventVO);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logging.getInstance().log(getClass(), e, Level.ERROR);
		}
		
		return bsEvents;
	}

}
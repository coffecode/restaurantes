package codecoffe.restaurantes.utilitarios;
import java.io.IOException;
import java.net.SocketTimeoutException;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class buscaCEP {

	public String getEndereco(String CEP) throws IOException {

		//***************************************************
		try{

			Document doc = Jsoup.connect("http://www.qualocep.com/busca-cep/"+CEP)
					.timeout(120000)
					.get();
			Elements urlPesquisa = doc.select("span[itemprop=streetAddress]");
			for (Element urlEndereco : urlPesquisa) {
				byte[] iso88591Data = urlEndereco.text().getBytes("ISO-8859-1");
				return new String(iso88591Data, "UTF-8");
			}

		} catch (SocketTimeoutException e) {

		} catch (HttpStatusException w) {

		}

		return CEP;
	}

	public String getBairro(String CEP) throws IOException {

		//***************************************************
		try{

			Document doc = Jsoup.connect("http://www.qualocep.com/busca-cep/"+CEP)
					.timeout(120000)
					.get();
			Elements urlPesquisa = doc.select("td:gt(1)");
			for (Element urlBairro : urlPesquisa) {
				byte[] iso88591Data = urlBairro.text().getBytes("ISO-8859-1");
				return new String(iso88591Data, "UTF-8");
			}

		} catch (SocketTimeoutException e) {

		} catch (HttpStatusException w) {

		}
		return CEP;
	}

	public String getCidade(String CEP) throws IOException {

		//***************************************************
		try{

			Document doc = Jsoup.connect("http://www.qualocep.com/busca-cep/"+CEP)
					.timeout(120000)
					.get();
			Elements urlPesquisa = doc.select("span[itemprop=addressLocality]");
			for (Element urlCidade : urlPesquisa) {
				byte[] iso88591Data = urlCidade.text().getBytes("ISO-8859-1");
				return new String(iso88591Data, "UTF-8");
			}

		} catch (SocketTimeoutException e) {

		} catch (HttpStatusException w) {

		}
		return CEP;
	}

	public String getUF(String CEP) throws IOException {

		//***************************************************
		try{

			Document doc = Jsoup.connect("http://www.qualocep.com/busca-cep/"+CEP)
					.timeout(120000)
					.get();
			Elements urlPesquisa = doc.select("span[itemprop=addressRegion]");
			for (Element urlUF : urlPesquisa) {
				return urlUF.text();
			}

		} catch (SocketTimeoutException e) {

		} catch (HttpStatusException w) {

		}
		return CEP;
	}
}
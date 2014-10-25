package de.aima13.whoami;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import org.stringtemplate.v4.ST;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by D060469 on 16.10.14.
 */
public class ReportCreator {
	private List<Representable> representableList;
	private final String TEMPLATE_LOCATION = "/data/ReportTemplate.html";
	private String htmlContent;

	

	/**
	 * Constructor
	 * @param representables Liste aller Representables, die in den Bericht aufgenommen werden
	 */
	public ReportCreator(List<Representable> representables) {
		this.representableList = representables;
	}


	public void savePdf() throws Exception {
		com.itextpdf.text.Document pdfFile = new Document(PageSize.A4);



	}

	/**
	 * Erzeuge nur wenn nötig den HTML-Code und speicher diesen
	 * @return der HTML-Code
	 */
	public String getHtml() {
		if (this.htmlContent == null) {
			this.htmlContent = this.createHtml();
		}
		return this.htmlContent;
	}

	/**
	 * Lade HTML Template aus den Resourcen und fülle es mit den Daten der Representables
	 * Rendere danach das Template
	 * @return Der HTML-Code des Berichts
	 */
	private String createHtml() {
		// Template laden
		ST template = new ST(
				new Scanner(this.getClass().getResourceAsStream(TEMPLATE_LOCATION),
						"UTF-8").useDelimiter("\\A").next()
				, '$', '$'
		);

		// Daten der Representables einfügen
		for (Representable representable : representableList) {
			// Titel holen, wenn null den Klassennamen wählen
			String reprTitle = representable.getReportTitle();
			if (reprTitle == null) {
				reprTitle = representable.getClass().getSimpleName();
			}

			// HTML-Content holen, wenn null Standardtext einfügen
			String reprContent = representable.getHtml();
			if (reprContent == null) {
				reprContent = "-- Kein Inhalt verfügbar für Modul " + reprTitle + "--";
			}

			// Variablen hinzufügen
			template.addAggr("modules.{title, content}", reprTitle, reprContent);
		}

		// Template rendern und zurückgeben
		return template.render();
	}

	/**
	 * Lade alle HTML-Snippets der Representables
	 * @return Liste der HTML-Snippets
	 */
	private List<String> getHtmlSnippets() {
		List<String> htmlSnippets = new ArrayList<>();

		for (Representable representable : this.representableList) {
			htmlSnippets.add(representable.getHtml());
		}

		return htmlSnippets;
	}
}

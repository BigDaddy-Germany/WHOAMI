package de.aima13.whoami;

import de.aima13.whoami.support.Utilities;
import org.stringtemplate.v4.ST;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Erstellen und Speichern des Reportes aus allen Representables
 *
 * @author Marco Dörfler
 */
public class ReportCreator {
	private List<Representable> representableList;
	private String scanId;
	private final String TEMPLATE_LOCATION = "/report/ReportTemplate.html"; // Berichtsvorlage
	private final String FILE_NAME = "WHOAMI_Analyze_Results-%ID%.pdf"; // Name der PDF Datei
	private final String PLACEHOLDER_SCAN_ID = "%ID%";
	private String htmlContent;

	//private final String PDF_AUTHOR = "BigDaddy Analyst Group"; // Author der PDF
	//private final String PDF_TITLE = "Analysebericht"; // Titel der PDF (Meta-Daten)


	/**
	 * Constructor
	 *
	 * @param representables Liste aller Representables, die in den Bericht aufgenommen werden
	 */
	public ReportCreator(List<Representable> representables, String scanId) {
		this.representableList = representables;
		this.scanId = scanId;
	}


	/**
	 * HTML parsen und als HTML speichern
	 * Vorsicht: Hat nur ein Modul fehlerhaften HTML-Code kann kein Report gespeichert werden!
	 *
	 * @return Erfolgsmeldung: PDF gespeichert oder nicht
	 */
	public boolean savePdf() {
		try {
			// Evt. vorhandene Datei umbenennen
			Path outputFile = Paths.get(FILE_NAME.replace(PLACEHOLDER_SCAN_ID, this.scanId));
			if (Files.exists(outputFile)) {
				String newName = Utilities.getNewFileName(outputFile.getFileName().toString());
				Files.move(outputFile, Paths.get(newName));
			}
			// Wenn wir hier gelandet sind, können wir davon ausgehen, dass die Datei "frei" ist

			//PDF-Creator erstellen, HTML ausbessern und Dokument generieren lassen
			PdfEngine creator = new PdfEngine();
			String htmlSource = Utilities.convertHtmlToXhtml(this.getHtml());
			creator.generatePdf(htmlSource, outputFile);
			System.out.println("PDF created");

			// Wenn bis hier alles durchgelaufen ist, war es erfolgreich
			return true;

		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Erzeuge nur wenn nötig den HTML-Code und speicher diesen
	 *
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
	 *
	 * @return Der HTML-Code des Berichts
	 */
	private String createHtml() {
		// Template laden
		ST template = new ST(Utilities.getResourceAsString(TEMPLATE_LOCATION), '$', '$');

		// Daten der Representables einfügen
		for (Representable representable : representableList) {
			// Titel holen, wenn null den Klassennamen wählen
			String reprTitle = representable.getReportTitle();
			if (reprTitle == null) {
				reprTitle = representable.getClass().getSimpleName();
			}

			// HTML-Content holen, wenn null Standardtext einfügen
			String reprContent = representable.getHtml();
			if (reprContent != null) {
				// Variablen hinzufügen
				template.addAggr("modules.{title, content}", reprTitle, reprContent);
			}
		}

		// Template rendern und zurückgeben
		return template.render();
	}

	/**
	 * Lade alle HTML-Snippets der Representables
	 *
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

package de.aima13.whoami;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.exceptions.RuntimeWorkerException;
import de.aima13.whoami.support.Utilities;
import org.stringtemplate.v4.ST;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Marco Dörfler on 16.10.14.
 * Erstellen und Speichern des Reportes aus allen Representables
 */
public class ReportCreator {
	private List<Representable> representableList;
	private final String TEMPLATE_LOCATION = "/data/ReportTemplate.html"; // Location des Templates
	private final String FILE_NAME = "WHOAMI_Analyze_Results.pdf"; // Name der PDF Datei
	private String htmlContent;

	private final String PDF_AUTHOR = "BigDaddy Analyst Group"; // Author der PDF
	private final String PDF_TITLE = "Analysebericht"; // Titel der PDF (Meta-Daten)



	/**
	 * Constructor
	 * @param representables Liste aller Representables, die in den Bericht aufgenommen werden
	 *
	 * @author Marco Dörfler
	 */
	public ReportCreator(List<Representable> representables) {
		this.representableList = representables;
	}


	/**
	 * HTML parsen und als HTML speichern
	 * Vorsicht: Hat nur ein Modul fehlerhaften HTML-Code kann kein Report gespeichert werden!
	 * @return Erfolgsmeldung: PDF gespeichert oder nicht
	 *
	 * @author Marco Dörfler
	 */
	public boolean savePdf() {
		try {
			// Evt. vorhandene Datei umbenennen
			Path outputFile = Paths.get(FILE_NAME);
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
	 * @return der HTML-Code
	 *
	 * @author Marco Dörfler
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
	 *
	 * @author Marco Dörfler
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
	 *
	 * @author Marco Dörfler
	 */
	private List<String> getHtmlSnippets() {
		List<String> htmlSnippets = new ArrayList<>();


		for (Representable representable : this.representableList) {
			htmlSnippets.add(representable.getHtml());
		}

		return htmlSnippets;
	}
}

package de.aima13.whoami;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import de.aima13.whoami.support.Utilities;
import org.stringtemplate.v4.ST;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by D060469 on 16.10.14.
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
	 */
	public ReportCreator(List<Representable> representables) {
		this.representableList = representables;
	}


	public boolean savePdf() {
		try {
			// Evt. vorhandene Datei umbenennen
			Path outputFile = Paths.get(FILE_NAME);
			if (Files.exists(outputFile)) {
				String newName = Utilities.getNewFileName(outputFile.getFileName().toString());
				Files.move(outputFile, Paths.get(newName));
			}
			// Wenn wir hier gelandet sind, können wir davon ausgehen, dass die Datei "frei" ist

			// Dokument erstellen und PDF-Writer erzeugen
			Document pdfFile = new Document(PageSize.A4);
			PdfWriter writer;
			writer = PdfWriter.getInstance(pdfFile, Files.newOutputStream(outputFile));

			// Dokument öffnen
			pdfFile.open();
			pdfFile.newPage();

			// Metadaten hinzufügen
			pdfFile.addAuthor(PDF_AUTHOR);
			pdfFile.addTitle(PDF_TITLE);
			pdfFile.addCreationDate();

			// HTML-Worker erstellen, um HTML-Content zu schreiben
			String htmlSource = this.getHtml();
			//htmlSource = "<p>Hallo Welt.</p>";
			ByteArrayInputStream htmlSourceInputStream = new ByteArrayInputStream(
					htmlSource.getBytes(StandardCharsets.UTF_8.toString())
			);
			XMLWorkerHelper.getInstance().parseXHtml(writer, pdfFile, htmlSourceInputStream);

			// Dokument schließen
			pdfFile.close();

			System.out.println("PDF Created!");

			// Wenn bis hier alles durchgelaufen ist, war es erfolgreich
			return true;


		} catch (IOException e) {
			return false;
		} catch (DocumentException e) {
			return false;
		}
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
						StandardCharsets.UTF_8.toString()).useDelimiter("\\A").next()
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

		String htmlCode = template.render();

		System.out.println("HTML Created:");
		System.out.println("-----------------");
		System.out.println(htmlCode);
		System.out.println("\n\n");

		// Template rendern und zurückgeben
		return htmlCode;
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

package de.aima13.whoami;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Wrapper für den verwendete PDF-Creator, welche aus HTML-Quelltext ein PDF generiert
 *
 * @author Niko Berkmann
 */
public class PdfEngine implements AutoCloseable {

	Path enginePath = null;

	/**
	 * Entpackt den PDF-Creator temporär auf die Festplatte
	 *
	 * @throws IOException
	 */
	PdfEngine() throws IOException {
		InputStream packedProgram = Whoami.class.getResourceAsStream("/report/wkhtmltopdf.exe");
		enginePath = Files.createTempFile("wkhtmltopdf", ".exe");

		Files.copy(packedProgram, enginePath, StandardCopyOption.REPLACE_EXISTING);
	}

	/**
	 * Generiert eine PDF-Datei aus HTML-Quelltext
	 *
	 * @param html   Quelltext
	 * @param output Pfad der Zieldatei
	 * @throws IOException Fehler von Dateisystemnatur bei der Berichtserstellung
	 */
	public void generatePdf(String html, Path output) throws IOException {
		try {

			//HTML-Quelltext in Datei speichern
			Path tempHtml = Files.createTempFile("whoami-report", ".html");
			Files.write(tempHtml, html.getBytes(StandardCharsets.UTF_8));

			//PDF im temporären Ordner generieren und anschließend an Zielort verschieben
			Path tempPdf = Files.createTempFile("whoami-report", ".pdf");
			Process engine = new ProcessBuilder(enginePath.toAbsolutePath().toString(),
					tempHtml.toAbsolutePath().toString(),
					tempPdf.toAbsolutePath().toString()).start();
			Files.move(tempPdf, output);

			//Temporäre Dateien wieder löschen
			Files.delete(tempHtml);
			Files.delete(tempPdf);

		} catch (Exception e) { //Fehler verallgemeinern
			e.printStackTrace();
			throw new IOException("Der PDF-Bericht konnte nicht generiert werden!");
		}
	}

	/**
	 * Löscht die temporäre Programmdatei, danach ist die PdfEngine nicht mehr benutzbar
	 *
	 * @throws IOException Temporäre Programmdatei kann nicht gelöscht werden
	 */
	@Override
	public void close() throws IOException {
		Files.delete(enginePath);
	}
}

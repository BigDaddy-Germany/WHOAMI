package de.aima13.whoami;

import de.aima13.whoami.support.Utilities;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Wrapper für den verwendeten PDF-Creator, welcher aus HTML-Quelltext ein PDF generiert
 *
 * @author Niko Berkmann
 */
public class PdfEngine {

	Path enginePath = null;

	/**
	 * Entpackt den PDF-Creator temporär auf die Festplatte
	 *
	 * @throws IOException
	 */
	PdfEngine() throws IOException {
		InputStream packedProgram = Whoami.class.getResourceAsStream("/report/wkhtmltopdf.exe");
		enginePath = Files.createTempFile("wkhtmltopdf", ".exe");
		Utilities.deleteTempFileOnExit(enginePath.toAbsolutePath().toString());

		Files.copy(packedProgram, enginePath, StandardCopyOption.REPLACE_EXISTING);
	}

	/**
	 * Generiert eine PDF-Datei aus HTML-Quelltext
	 *
	 * @param html   Quelltext
	 * @param output Pfad der Zieldatei, wird überschrieben falls schon vorhanden
	 * @throws IOException Fehler von Dateisystemnatur bei der Berichtserstellung
	 */
	public void generatePdf(String html, Path output) throws IOException {
		if (enginePath == null){
			throw new RuntimeException("PdfCreater nach close() benutzt");
		}

		try {

			//HTML-Quelltext in Datei speichern
			Path tempHtml = Files.createTempFile("whoami-report", ".html");
			Utilities.deleteTempFileOnExit(tempHtml.toAbsolutePath().toString());

			Files.write(tempHtml, html.getBytes(StandardCharsets.UTF_8));

			//PDF im temporären Ordner generieren und anschließend an Zielort verschieben
			Path tempPdf = Files.createTempFile("whoami-report", ".pdf");
			Utilities.deleteTempFileOnExit(tempPdf.toAbsolutePath().toString());

			Process engine = new ProcessBuilder(enginePath.toAbsolutePath().toString(),
					tempHtml.toAbsolutePath().toString(),
					tempPdf.toAbsolutePath().toString()).start();
			engine.waitFor();

			Files.move(tempPdf, output, StandardCopyOption.REPLACE_EXISTING);

		} catch (Exception e) { //Fehler verallgemeinern
			e.printStackTrace();
			throw new IOException("Der PDF-Bericht konnte nicht generiert werden!");
		}
	}
}

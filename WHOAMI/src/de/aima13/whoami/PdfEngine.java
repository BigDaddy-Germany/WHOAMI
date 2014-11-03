package de.aima13.whoami;

import java.io.IOException;
import java.io.InputStream;
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
	 * Löscht die temporäre Programmdatei, danach ist die PdfEngine nicht mehr benutzbar
	 *
	 * @throws IOException Temporäre Programmdatei kann nicht gelöscht werden
	 */
	@Override
	public void close() throws IOException {
		Files.delete(enginePath);
	}
}

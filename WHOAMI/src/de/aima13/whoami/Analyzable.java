package de.aima13.whoami;

import java.io.File;
import java.util.List;

/**
 * Created by D060469 on 16.10.14.
 */
public interface Analyzable extends Runnable, Representable {
	public List<String> getFilter();

	public void setFileInputs(List<File> files) throws Exception;
}

package de.aima13.whoami;

/**
 * Created by D060469 on 16.10.14.
 */
public interface Analyzable extends Runnable, Representable {
    public void getFilter();
    public void setFileInputs();
}

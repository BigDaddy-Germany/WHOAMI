package de.aima13.whoami;

import java.util.SortedMap;

/**
 * Created by D060469 on 16.10.14.
 */
public interface Representable {
    public String getHtml();

    public SortedMap<String, String> getCsvContent();
}

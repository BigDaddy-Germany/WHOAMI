package de.aima13.whoami.modules.music;

import de.aima13.whoami.GlobalData;

/**
 * Created by Inga on 24.11.2014.
 */
public class MusicTextOutput {

	String stmtGenre = "";

	/**
	 * Ordnet dem Genre eine Art Kategorie zu, wie im Architekturdokument angekündigt. Dazu
	 * wird zu jeder Kategory ein Kommentar, "hardcoded" als String hinzugefügt,
	 * um einen Fließtext im PDF-Dokument zu erhalten.
	 * @return String stmtGenre
	 */
	public String getCategory(String favGenre) {
		StringBuilder statementToGenre = new StringBuilder();

		if (favGenre.equals("Top 40") || favGenre.equals("House") || favGenre.equals("Drum & " +
				"Bass") || favGenre.equals("Euro-House")) {
			statementToGenre.append("<br />Dein Musikgeschmack ist nicht gerade " +
					"aussagekräftig.<br />Du scheinst nicht wirklich auszuwählen was " +
					"dir gefällt,<br />sondern orientierst dich an 'Top-Lists' und Freunden.<br />" +
					"Was deinen Musikgeschmack charaktierisitert ist wahrscheinlich das " +
					"Mainstream-Opfer.");
		} else if (favGenre.equals("Dance") || favGenre.equals("Disco") || favGenre.equals("Dancehall")
				|| favGenre.equals("Samba") || favGenre.equals("Tango") || favGenre.equals("Club") ||
				favGenre.equals("Swing") || favGenre.equals("Latin") || favGenre.equals("Salsa")
				|| favGenre.equals("Eurodance") || favGenre.equals("Pop")) {
			statementToGenre.append("<br />Deinem Musikstil, " + favGenre + ", " +
					"nach zu urteilen,<br />schwingst du zumindest gerne dein Tanzbein oder bist " +
					"sogar eine richtige Dancing Queen! <3");
		} else if (favGenre.equals("Techno") || favGenre.equals("Industrial") || favGenre.equals
				("Acid Jazz") || favGenre.equals("Rave") || favGenre.equals("Psychedelic") ||
				favGenre.equals("Dream") || favGenre.equals("Elecronic") || favGenre
				.equals("Techno-Industrial") || favGenre.equals("Space") || favGenre.equals("Acid")
				|| favGenre.equals("Trance") || favGenre.equals("Fusion") ||
				favGenre.equals("Euro-Techno") || favGenre.equals("Hardcore Techno") || favGenre
				.equals("Goa") || favGenre.equals("Fast Fusion") || favGenre.equals("Synthpop") ||
				favGenre.equals("Dub") || favGenre.equals("Psytrance") || favGenre.equals
				("Dubstep") || favGenre.equals("Psybient")) {
			statementToGenre.append("<br />Dein Musikstil lässt darauf schließen, " +
					"<br />dass wenn man dich grob einer Richtung zuordnet du am ehesten einem Raver " +
					"entsprichst. Ich denke die Erläuterung der entsprechenden Klischees muss an " +
					"dieser Stelle nicht aufgeklärt werden ;-)");
		} else if (favGenre.equals("Retro") || favGenre.equals("Polka") || favGenre.equals
				("Country") || favGenre.equals("Oldies") || favGenre.equals("Native US") ||
				favGenre.equals("Southern Rock") || favGenre.equals("Instrumental") || favGenre
				.equals("Classical") || favGenre.equals("Gospel") || favGenre.equals("Folklore") ||
				favGenre.equals("A capella") || favGenre.equals("Symphony") ||
				favGenre.equals("Sonata") || favGenre.equals("Opera") || favGenre.equals
				("National Folk") || favGenre.equals("Avantgarde") || favGenre.equals("Baroque") ||
				favGenre.equals("World Music") || favGenre.equals("Neoclassical")) {
			statementToGenre.append("<br />Dein Musikstil" + favGenre + " ist eher von " +
					"traditioneller Natur und verrät uns, dass du in der Zeit stehen geblieben bist.");
		} else if (favGenre.equals("Christian Rap") || favGenre.equals("Pop-Folk") || favGenre
				.equals("Christian Rock") || favGenre.equals("Contemporary Christian") ||
				favGenre.equals("Christian Gangsta Rap") || favGenre.equals("Terror") || favGenre
				.equals("Jpop") || favGenre.equals("Math Rock") || favGenre.equals("Emo") ||
				favGenre.equals("New Romantic")) {
			statementToGenre.append("<br />Über Geschmack lässt sich ja bekanntlich streiten. " +
					"Aber " + favGenre + " - Dein Ernst?! Da fällt selbst uns nichts mehr zu ein.");
		} else if (favGenre.equals("Post-Rock") || favGenre.equals("Classic Rock") || favGenre
				.equals("Metal") || favGenre.equals("Rock") || favGenre.equals("Death Metal") ||
				favGenre.equals("Hard Rock") || favGenre.equals("Alternative Rock") || favGenre
				.equals("Instrumental Rock") || favGenre.equals("Darkwave") || favGenre.equals
				("Gothic") || favGenre.equals("Folk Rock") ||
				favGenre.equals("Symphonic Rock") || favGenre.equals("Gothic Rock") || favGenre
				.equals("Progressive Rock") || favGenre.equals("Black Metal") || favGenre.equals
				("Heavy Metal") || favGenre.equals("Punk Rock") || favGenre.equals("Rythmic " +
				"Soul") || favGenre.equals("Thrash Metal") || favGenre.equals("Garage Rock") ||
				favGenre.equals("Space Rock") || favGenre.equals("Industro-Goth") || favGenre
				.equals("Garage") || favGenre.equals("Art Rock")) {
			statementToGenre.append(" " + favGenre + "? In dir steckt bestimmt ein Headbanger " +
					"(irgendwo)! Yeah" + "\\m/ !!!");
		} else if (favGenre.equals("Chillout") || favGenre.equals("Reggea") || favGenre.equals
				("Trip-Hop") || favGenre.equals("Hip-Hop")) {
			statementToGenre.append("<br />Deine Szene ist wahrscheinlich die Hip Hop Szene.<br />Du bist ein " +
					"sehr relaxter Mensch <br />und vermutlich gehören die Baggy Pants " +
					"zu deinen Lieblingskleidungstücken?");
		} else if (favGenre.equals("Blues") || favGenre.equals("Jazz") || favGenre.equals("Vocal")
				|| favGenre.equals("Jazz & Funk") || favGenre.equals("Soul") || favGenre.equals
				("Ambient") || favGenre.equals("Illbient") || favGenre.equals("Lounge")) {
			statementToGenre.append("<br />Deinem Lieblingsgenre zu urteilen beschreibt sich dieses " +
					"Modul als wahren Kenner.<br />Vermutlich spielst du selber mindestens ein " +
					"Instrument <br />und verbringt dein Leben am liebsten entspannt mit einem " +
					"Glas Rotwein.");
		} else if (favGenre.equals("Gangsta") || favGenre.equals("Rap")) {
			statementToGenre.append("<br />Du hörst Rap. Vielleicht bis du sogar ein übler " +
					"Gangstarapper. Dir sollte man lieber nicht im Dunkeln begegnen...");
		} else if (favGenre.equals("Ska") || favGenre.equals("Acid Punk") || favGenre.equals("Punk")
				|| favGenre.equals("Polsk Punk") || favGenre.equals("Negerpunk") || favGenre
				.equals("Post-Punk")) {
			statementToGenre.append("<br />Deine Musiklieblingsrichtung ist Punk oder " +
					"zumindest eine eine Strömung des Punks. Demnach bist du ein echter " +
					"Rebell: 'Wir sind Punks und wir " +
					"sind frei. Du bist bei der Polizei. Wir sind der Untergang der " +
					"Zivilisation!'");
		} else if (favGenre.equals("Funk") || favGenre.equals("New Age") || favGenre.equals
				("Grunge") || favGenre.equals("New Wave") || favGenre.equals("Rock & Roll") ||
				favGenre.equals("BritPop") || favGenre.equals("Indie") || favGenre.equals("Porn " +
				"Groove") || favGenre.equals("Chanson") || favGenre.equals("Folk") || favGenre
				.equals("Experimental") || favGenre.equals("Neue Deutsche Welle") || favGenre
				.equals("Indie Rock") || favGenre.equals("Alternative")) {
			statementToGenre.append("<br />Dein Musikgeschmack, " + favGenre + ", " +
					"zeugt auf jeden Fall von Geschmack und Stil. Käme es nur auf den " +
					"Musikgeschmack an sollte man dich auf jeden Fall kennen lernen.");
		} else if (favGenre.equals("Podcast") || favGenre.equals("Audio Theatre") || favGenre.equals
				("Audiobook") || favGenre.equals("Speech") || favGenre.equals("Satire") ||
				favGenre.equals("Soundtrack") || favGenre.equals("Sound Clip") || favGenre.equals
				("Comedy") || favGenre.equals("Cabaret") || favGenre.equals("Showtunes") ||
				favGenre.equals("Trailer") || favGenre.equals("Musical")) {
			statementToGenre.append("<br />Die Audiodatei lässt sich einer Art Literatur zuordnen. " +
					"<br />Du bist entweder sehr Literaturbegeistert und liebst Soundtracks und Co" +
					"<br />oder eine sehr faule Leseratte, die sich lieber alles vorlesen lässt. <br />" +
					"Wie auch immer du bist, " +
					"wahrscheinlich ein ziemlich belesener Mensch. ");
		} else {
			statementToGenre.append("<br />Dein Musikgeschmack " + favGenre + " <br />ist auf jeden " +
					"Fall ziemlich " +
					"extravagant. Ob im positiven oder negativen Sinne lassen wir hier mal offen." +
					"..");
		}
		//Da hier globale Scores verändert werden, folgt hier ein zusätzlicher Kommentar damit
		// der User am Ende weiß, wie sich seine Punktzahl zusammen setzt
		if (favGenre.equals("Emo")) {
			GlobalData.getInstance().changeScore("Selbstmordgefährdung", 80);
			statementToGenre.append(" Aufgrund deines Lieblingsgenres sehen wir " +
					"eine deutliche erhöhte Selbstmordgefahr!");
		}
		if (favGenre.equals("Games")) {
			GlobalData.getInstance().changeScore("Nerdfaktor", 70);
			statementToGenre.append(" Aufgrund deines Lieblingsgenres haben wir den Nerdfaktor" +
					" um 70 erhöht.");
		}

		stmtGenre = statementToGenre.toString();
		return stmtGenre;
	}

	/**
	 * Hier wird ein Kommentar zur Anzahl der Musikdateien abgegeben
	 * @param nrAudio
	 * @return
	 */
		public String commentToAmmount(long nrAudio){
		String nrAudioCom = "";
		if (nrAudio > 200) {
			nrAudioCom = " Eine Menge Futter für das Modul! <br />";
		}
		if (nrAudio > 100 && nrAudio <= 200) {
			nrAudioCom = " Schon eine Menge mit der das Modul einiges anfangen kann! <br />";
		}
		if (nrAudio <= 100) {
			nrAudioCom = " Immerhin ein bischen Input.<br />";
		}
		return nrAudioCom;
	}

	/**
	 * Hier wird das HTML-Output für die PDF-Datei als String zusammengesetzt
	 * @param cltProgram
	 * @param onlService
	 * @param favArtist
	 * @param favGenre
	 * @param nrAudio
	 * @param Qualität
	 * @return
	 */
	public String html(String cltProgram, String onlService, String favArtist, String favGenre,
	                   long nrAudio, String Qualität){
		StringBuilder buffer = new StringBuilder();

		// ', ' aus cltprogram und onlService durch Zeilenumbrüche ergänzen

		cltProgram = cltProgram.replaceAll(", ", "<br \\>");
		onlService = onlService.replaceAll(", ", "<br \\>");

		// Ergebnistabelle
		if (!(favGenre.equals("") && onlService.equals("") && cltProgram.equals("") && favArtist
				.equals(""))) {
			buffer.append("<table>");
			if (!(favArtist.equals(""))) {
				buffer.append("<tr><td>Lieblingskünstler:</td>" +
						"<td>" + favArtist + "</td></tr>");
			}
			if (!(favGenre.equals(""))) {
				buffer.append("<tr>" +
						"<td>Lieblingsgenre:</td>" +
						"<td>" + favGenre + "</td>" +
						"</tr>");
			}
			if (!(cltProgram.equals(""))) {
				buffer.append("<tr>" +
						"<td>Musikprogramme:</td>" +
						"<td>" + cltProgram + "</td>" +
						"</tr>");
			}
			if (!(onlService.equals(""))) {
				buffer.append("<tr>" +
						"<td>Onlinestreams:</td>" +
						"<td>" + onlService + "</td>" +
						"</tr>");
			}
			buffer.append("</table>");
		} else {
			buffer.append("Es wurden keine Informationen gefunden um den scheinbar " +
					"sehr geheimen Musikgeschmack des Users zu analysieren. Entweder dein besitzt" +
					" du erschreckender Weise kein Interesse für Musik oder du konsumierst deine" +
					" Musik über einen anderen Weg.");
		}

		// Abschlussfazit des Musikmoduls
		if (!(onlService.equals("")) && !(favArtist.equals("")) && !(favGenre.equals(""))
				&& !(cltProgram.equals(""))) {
			buffer.append("<br /><b>Fazit:</b> Dein Computer enthält Informationen zu allem " +
					"was wir gesucht haben. <br />Musik scheint ein wichtiger Teil deines Lebens " +
					"zu sein. Und tatsächlich ist sogar wissenschaftlich bewiesen, " +
					"dass Musik uns im tiefsten Inneren berührt, von Geburt an prägt und zu " +
					"Höchstleistungen treibt. Kurz: Musik kann eine Menge über einen Menschen " +
					"verraten!<br />" + "Insgesamt " +
					"haben wir ganze " + nrAudio + " Musikdateien gefunden.");
			buffer.append(commentToAmmount(nrAudio));
			if (!(Qualität.equals(""))) {
				buffer.append(Qualität + " ");
			}
			buffer.append(stmtGenre);
		} else if (onlService.equals("") && cltProgram.equals("") && !(favGenre.equals(""))) {
			buffer.append("<br /><b>Fazit:</b> Es ist wissenschaftlich bewiesen, dass " +
					"Musik uns im tiefsten Inneren berührt, von Geburt an prägt und uns sogar " +
					"Höchstleistungen treibt. Umso besser, dass wir deinen Geschmack " +
					"erfolgreich analysieren konnten:<br /> Insgesamt haben wir " + nrAudio + " " +
					"Musikdateien " +
					"gefunden." + Qualität + stmtGenre + ". Du benutzt wohl irgendeinen " +
					"anderen Musikplayer um deine Musik zu hören. Was wir wissen: Spotify, " +
					"iTunes und Co sind es nicht!");
		} else if (favGenre.equals("") && favArtist.equals("")) {
			buffer.append("<br /><b>Fazit:</b> Es konnten keine Informationen dazu gefunden " +
					"werden was du hörst. Deine Lieblingsgenre und Lieblingkünstler bleiben " +
					"leider eine offene Frage... Schade, wenn man bedenkt, " +
					"dass Musik so viel über Menschen verraten kann. ");
			if (!(onlService.equals("")) && !(cltProgram.equals(""))) {
				buffer.append(" Immerhin gehen wir nicht ganz leer aus: Worrüber du Musik hörst " +
						"konnten wir wie in der Tabelle zu sehen in Erfahrung bringen.");
				if (nrAudio != 0) {
					buffer.append("Insgesamt haben wir zusätzlich " + nrAudio + " Musikdateien " +
							"gefunden, die leider keine Analyse zulassen." + Qualität);
				}
			}
		} else {
			buffer.append("<br /><b>Fazit:</b> Zwar konnten einige Informationen über " +
					"dich nicht herausgefunden werden, <br />aber einiges wissen wir.");
			if (nrAudio > 0) {
				buffer.append("<br />Insgesamt haben wir " + nrAudio + " Musikdateien gefunden." +
						Qualität);
			}
			if (nrAudio > 200) {
				buffer.append(" Eine Menge Futter für das Modul! <br />");
			}
			if (nrAudio > 100 && nrAudio <= 200) {
				buffer.append(" Schon eine Menge mit der das Modul einiges anfangen kann! <br />");
			}
			if (nrAudio <= 100) {
				buffer.append(" Immerhin ein bischen Input.<br />");
			}
			if (!(favArtist.equals(""))) {
				buffer.append("<br />Deine Lieblingsband ist " + favArtist + ".");
			}
			if (!(favGenre.equals(""))) {
				buffer.append("<br />" + stmtGenre);
			}
		}

		String html = buffer.toString();
	return html;
	}
}

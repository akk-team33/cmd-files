package de.team33.cmd.files.listing.publics;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NegativeLookaheadTrial {

    public static void main(String[] args) {
        String regex = "(?!\\.).*";
        Pattern pattern = Pattern.compile(regex);

        String[] testStrings = {"Hallo", "", ".test", "a.b", ".", "123", "...", " ..."};

        for (String s : testStrings) {
            Matcher matcher = pattern.matcher(s);
            System.out.println("\"" + s + "\" => " + matcher.matches());
        }
    }
}

package com.shagalalab.marshrutka.data;

import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by aziz on 7/10/15.
 */
public class DestinationPoint {
    private static final char[] QQ_ALPHABET = new char[] {
            'а', 'ә', 'б', 'в', 'г', 'ғ', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'қ', 'л', 'м',
            'н', 'ң', 'о', 'ө', 'п', 'р', 'с', 'т', 'у', 'ү', 'ў', 'ф', 'х', 'ҳ', 'ц', 'ч', 'ш',
            'щ', 'ъ', 'ы', 'ь', 'э', 'ю', 'я'
    };
    private static final HashMap<Character, Integer> QQ_BINDINGS = new HashMap<Character, Integer>();
    static {
        int len = QQ_ALPHABET.length;
        for (int i=0; i<len; i++) {
            QQ_BINDINGS.put(QQ_ALPHABET[i], i);
        }
    }

    public final int ID;
    public final String nameCyr, nameLat;

    // Same strings as previous ones, but specific qq letters changed to normal russian/english letters.
    // This is useful for autocomplete, as the input during autocomplete comes from
    // soft keyboard where these specific letters may not be present.
    // i.e. "ә" -> "а", "ғ" -> "г", "a'" to "a", "u'" -> "u"
    public final String nameCyrAlternative, nameLatAlternative;

    private final boolean isCyrillic;

    public DestinationPoint(boolean isCyrillic, int id, String nameCyr, String nameLat) {
        this.isCyrillic = isCyrillic;
        this.ID = id;
        this.nameCyr = nameCyr;
        this.nameLat = nameLat;
        this.nameCyrAlternative = generateAlternativeNameCyr(nameCyr);
        this.nameLatAlternative = generateAlternativeNameLat(nameLat);
    }

    public String getName() {
        return isCyrillic ? nameCyr : nameLat;
    }

    private String generateAlternativeNameCyr(String name) {
        return name.toLowerCase().replace('ә', 'а').replace('ғ', 'г').replace('қ', 'к')
                .replace('ң', 'н').replace('ө', 'о').replace('ү', 'у')
                .replace('ў', 'у').replace('ҳ', 'х');
    }

    private String generateAlternativeNameLat(String name) {
        return name.toLowerCase().replace("a'", "a").replace("g'", "g").replace("n'", "n")
                .replace("o'", "o").replace("u'", "u").replace("ı", "i");
    }

    public static final Comparator<DestinationPoint> QQ_CYR_COMPARATOR = new Comparator<DestinationPoint>() {
        @Override
        public int compare(DestinationPoint first, DestinationPoint second) {
            char[] thisLowerCase = first.nameCyr.toLowerCase().toCharArray();
            char[] thatLowerCase = second.nameCyr.toLowerCase().toCharArray();

            int len = Math.min(thisLowerCase.length, thatLowerCase.length);
            for (int i=0; i<len; i++) {
                char thisChar = thisLowerCase[i];
                char thatChar = thatLowerCase[i];
                Integer thisIndex = QQ_BINDINGS.get(thisChar);
                Integer thatIndex = QQ_BINDINGS.get(thatChar);
                if (thisIndex == null && thatIndex == null) {
                    if (thisChar == thatChar) {
                        continue;
                    } else {
                        return thisChar - thatChar;
                    }
                } else if (thisIndex == null) {
                    return thisChar - (int)'а';
                } else if (thatIndex == null) {
                    return (int)'а' - thatChar;
                } else if (thatIndex.equals(thisIndex)) {
                    continue;
                } else {
                    return thisIndex - thatIndex;
                }
            }
            return first.nameCyr.compareTo(second.nameCyr);
        }
    };

    public static final Comparator<DestinationPoint> QQ_LAT_COMPARATOR = new Comparator<DestinationPoint>() {
        @Override
        public int compare(DestinationPoint first, DestinationPoint second) {
            return first.nameLat.compareTo(second.nameLat);
        }
    };

    @Override
    public boolean equals(Object that) {
        if (that == null) return false;

        if (!(that instanceof DestinationPoint)) return false;

        DestinationPoint thatPoint = (DestinationPoint)that;
        return this.ID == thatPoint.ID && this.nameCyr.equals(thatPoint.nameCyr);
    }

    @Override
    public String toString() {
        return getName();
    }
}
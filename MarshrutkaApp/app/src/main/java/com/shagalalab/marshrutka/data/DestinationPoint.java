package com.shagalalab.marshrutka.data;

import java.util.HashMap;

/**
 * Created by aziz on 7/10/15.
 */
public class DestinationPoint implements Comparable<DestinationPoint> {
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

    public int ID;
    public String name;

    public DestinationPoint() {
    }

    public DestinationPoint(int id, String name) {
        this.ID = id;
        this.name = name;
    }

    @Override
    public int compareTo(DestinationPoint that) {
        char[] thisLowerCase = this.name.toLowerCase().toCharArray();
        char[] thatLowerCase = that.name.toLowerCase().toCharArray();

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
            } else {
                return thisIndex - thatIndex;
            }
        }
        return this.compareTo(that);
    }

    @Override
    public boolean equals(Object that) {
        if (that == null) return false;

        if (!(that instanceof DestinationPoint)) return false;

        DestinationPoint thatPoint = (DestinationPoint)that;
        return this.ID == thatPoint.ID && this.name.equals(thatPoint.name);
    }

    public static void main(String[] args) {
        DestinationPoint p1 = new DestinationPoint(0, "Әжинияз");
        DestinationPoint p2 = new DestinationPoint(0, "Қосыбай");
        DestinationPoint p3 = new DestinationPoint(0, "Амангелди");
        DestinationPoint p4 = new DestinationPoint(0, "Буўрабай");
        DestinationPoint p5 = new DestinationPoint(0, "Камал");
        DestinationPoint p6 = new DestinationPoint(0, "Ғалым");
        DestinationPoint p7 = new DestinationPoint(0, "Ганс");
        DestinationPoint p8 = new DestinationPoint(0, "Дәрьябай");
        DestinationPoint p9 = new DestinationPoint(0, "Юра");

        assert p1.compareTo(p3) > 0;
        assert p1.compareTo(p4) < 0;
        assert p1.compareTo(p7) < 0;
        assert p2.compareTo(p5) > 0;
        assert p2.compareTo(p9) < 0;
        assert p6.compareTo(p8) < 0;
        assert p6.compareTo(p7) > 0;
        assert p9.compareTo(p6) > 0;
    }
}

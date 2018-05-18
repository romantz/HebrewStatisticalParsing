package train;

/**
 * Created by Roman_ on 18/05/2018.
 */
public class Pair<X, Y> {

    public final X x;
    public final Y y;

    public Pair(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Object p) {
        if (this == p)
            return true;
        else if (p == null || getClass() != p.getClass())
            return false;

        Pair<?, ?> pair2 = (Pair<?, ?>)p;
        if (!x.equals(pair2.x))
            return false;
        return y.equals(pair2.y);
    }

    public int hashCode() {
        int result = x.hashCode();
        result = 31 * result + y.hashCode();
        return result;
    }
}
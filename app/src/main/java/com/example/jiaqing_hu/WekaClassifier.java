package com.example.jiaqing_hu;

class WekaClassifier {

    public static double classify(Object[] i)
            throws Exception {

        double p = Double.NaN;
        p = WekaClassifier.N7ecd7af0(i);
        return p;
    }
    static double N7ecd7af0(Object []i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 0;
        } else if (((Double) i[0]).doubleValue() <= 110.672015) {
            p = WekaClassifier.N2676ed411(i);
        } else if (((Double) i[0]).doubleValue() > 110.672015) {
            p = WekaClassifier.N536ca51e4(i);
        }
        return p;
    }
    static double N2676ed411(Object []i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 0;
        } else if (((Double) i[0]).doubleValue() <= 86.387993) {
            p = 0;
        } else if (((Double) i[0]).doubleValue() > 86.387993) {
            p = WekaClassifier.N400f4fad2(i);
        }
        return p;
    }
    static double N400f4fad2(Object []i) {
        double p = Double.NaN;
        if (i[1] == null) {
            p = 1;
        } else if (((Double) i[1]).doubleValue() <= 25.177673) {
            p = WekaClassifier.N32dc5d453(i);
        } else if (((Double) i[1]).doubleValue() > 25.177673) {
            p = 0;
        }
        return p;
    }
    static double N32dc5d453(Object []i) {
        double p = Double.NaN;
        if (i[4] == null) {
            p = 0;
        } else if (((Double) i[4]).doubleValue() <= 9.353713) {
            p = 0;
        } else if (((Double) i[4]).doubleValue() > 9.353713) {
            p = 1;
        }
        return p;
    }
    static double N536ca51e4(Object []i) {
        double p = Double.NaN;
        if (i[64] == null) {
            p = 1;
        } else if (((Double) i[64]).doubleValue() <= 24.396242) {
            p = WekaClassifier.N426146d75(i);
        } else if (((Double) i[64]).doubleValue() > 24.396242) {
            p = 2;
        }
        return p;
    }
    static double N426146d75(Object []i) {
        double p = Double.NaN;
        if (i[3] == null) {
            p = 1;
        } else if (((Double) i[3]).doubleValue() <= 69.56357) {
            p = WekaClassifier.N78a90b116(i);
        } else if (((Double) i[3]).doubleValue() > 69.56357) {
            p = WekaClassifier.N7780a64715(i);
        }
        return p;
    }
    static double N78a90b116(Object []i) {
        double p = Double.NaN;
        if (i[11] == null) {
            p = 1;
        } else if (((Double) i[11]).doubleValue() <= 8.464121) {
            p = WekaClassifier.N284f47347(i);
        } else if (((Double) i[11]).doubleValue() > 8.464121) {
            p = WekaClassifier.N1ba80ff13(i);
        }
        return p;
    }
    static double N284f47347(Object []i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 1;
        } else if (((Double) i[0]).doubleValue() <= 281.653327) {
            p = WekaClassifier.N1ebd43768(i);
        } else if (((Double) i[0]).doubleValue() > 281.653327) {
            p = 1;
        }
        return p;
    }
    static double N1ebd43768(Object []i) {
        double p = Double.NaN;
        if (i[19] == null) {
            p = 1;
        } else if (((Double) i[19]).doubleValue() <= 0.952152) {
            p = 1;
        } else if (((Double) i[19]).doubleValue() > 0.952152) {
            p = WekaClassifier.N5622e9889(i);
        }
        return p;
    }
    static double N5622e9889(Object []i) {
        double p = Double.NaN;
        if (i[20] == null) {
            p = 0;
        } else if (((Double) i[20]).doubleValue() <= 0.760457) {
            p = 0;
        } else if (((Double) i[20]).doubleValue() > 0.760457) {
            p = WekaClassifier.N66e381de10(i);
        }
        return p;
    }
    static double N66e381de10(Object []i) {
        double p = Double.NaN;
        if (i[29] == null) {
            p = 1;
        } else if (((Double) i[29]).doubleValue() <= 0.882115) {
            p = 1;
        } else if (((Double) i[29]).doubleValue() > 0.882115) {
            p = WekaClassifier.N34470cd111(i);
        }
        return p;
    }
    static double N34470cd111(Object []i) {
        double p = Double.NaN;
        if (i[64] == null) {
            p = 0;
        } else if (((Double) i[64]).doubleValue() <= 6.302075) {
            p = WekaClassifier.N40155fdc12(i);
        } else if (((Double) i[64]).doubleValue() > 6.302075) {
            p = 1;
        }
        return p;
    }
    static double N40155fdc12(Object []i) {
        double p = Double.NaN;
        if (i[27] == null) {
            p = 1;
        } else if (((Double) i[27]).doubleValue() <= 0.896948) {
            p = 1;
        } else if (((Double) i[27]).doubleValue() > 0.896948) {
            p = 0;
        }
        return p;
    }
    static double N1ba80ff13(Object []i) {
        double p = Double.NaN;
        if (i[32] == null) {
            p = 0;
        } else if (((Double) i[32]).doubleValue() <= 2.523797) {
            p = 0;
        } else if (((Double) i[32]).doubleValue() > 2.523797) {
            p = WekaClassifier.N7bad1c6714(i);
        }
        return p;
    }
    static double N7bad1c6714(Object []i) {
        double p = Double.NaN;
        if (i[26] == null) {
            p = 2;
        } else if (((Double) i[26]).doubleValue() <= 4.645085) {
            p = 2;
        } else if (((Double) i[26]).doubleValue() > 4.645085) {
            p = 1;
        }
        return p;
    }
    static double N7780a64715(Object []i) {
        double p = Double.NaN;
        if (i[10] == null) {
            p = 1;
        } else if (((Double) i[10]).doubleValue() <= 15.787186) {
            p = WekaClassifier.N1d01ecd416(i);
        } else if (((Double) i[10]).doubleValue() > 15.787186) {
            p = 2;
        }
        return p;
    }
    static double N1d01ecd416(Object []i) {
        double p = Double.NaN;
        if (i[26] == null) {
            p = 2;
        } else if (((Double) i[26]).doubleValue() <= 3.686452) {
            p = 2;
        } else if (((Double) i[26]).doubleValue() > 3.686452) {
            p = 1;
        }
        return p;
    }
}

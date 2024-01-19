//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cn.ussshenzhou.villager.entity.fakeplayer.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author USS_Shenzhou
 * <br/>This file is modified from <a href="https://github.com/HorseNuggets/TerminatorPlus">TerminatorPlus</a> under EPL-2.0 license, and can be distributed under EPL-2.0 license only.
 */
public final class NumberConversions {
    private NumberConversions() {
    }

    public static int floor(double num) {
        int floor = (int)num;
        return (double)floor == num ? floor : floor - (int)(Double.doubleToRawLongBits(num) >>> 63);
    }

    public static int ceil(double num) {
        int floor = (int)num;
        return (double)floor == num ? floor : floor + (int)(~Double.doubleToRawLongBits(num) >>> 63);
    }

    public static int round(double num) {
        return floor(num + 0.5);
    }

    public static double square(double num) {
        return num * num;
    }

    public static int toInt(@Nullable Object object) {
        if (object instanceof Number) {
            return ((Number)object).intValue();
        } else {
            try {
                return Integer.parseInt(object.toString());
            } catch (NumberFormatException var2) {
            } catch (NullPointerException var3) {
            }

            return 0;
        }
    }

    public static float toFloat(@Nullable Object object) {
        if (object instanceof Number) {
            return ((Number)object).floatValue();
        } else {
            try {
                return Float.parseFloat(object.toString());
            } catch (NumberFormatException var2) {
            } catch (NullPointerException var3) {
            }

            return 0.0F;
        }
    }

    public static double toDouble(@Nullable Object object) {
        if (object instanceof Number) {
            return ((Number)object).doubleValue();
        } else {
            try {
                return Double.parseDouble(object.toString());
            } catch (NumberFormatException var2) {
            } catch (NullPointerException var3) {
            }

            return 0.0;
        }
    }

    public static long toLong(@Nullable Object object) {
        if (object instanceof Number) {
            return ((Number)object).longValue();
        } else {
            try {
                return Long.parseLong(object.toString());
            } catch (NumberFormatException var2) {
            } catch (NullPointerException var3) {
            }

            return 0L;
        }
    }

    public static short toShort(@Nullable Object object) {
        if (object instanceof Number) {
            return ((Number)object).shortValue();
        } else {
            try {
                return Short.parseShort(object.toString());
            } catch (NumberFormatException var2) {
            } catch (NullPointerException var3) {
            }

            return 0;
        }
    }

    public static byte toByte(@Nullable Object object) {
        if (object instanceof Number) {
            return ((Number)object).byteValue();
        } else {
            try {
                return Byte.parseByte(object.toString());
            } catch (NumberFormatException var2) {
            } catch (NullPointerException var3) {
            }

            return 0;
        }
    }

    public static boolean isFinite(double d) {
        return Math.abs(d) <= Double.MAX_VALUE;
    }

    public static boolean isFinite(float f) {
        return Math.abs(f) <= Float.MAX_VALUE;
    }

    public static void checkFinite(double d, @NotNull String message) {
        if (!isFinite(d)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void checkFinite(float d, @NotNull String message) {
        if (!isFinite(d)) {
            throw new IllegalArgumentException(message);
        }
    }
}

package edu.kit.informatik.network;


import edu.kit.informatik.exceptions.ParseException;

/**
 * the class IP models an IP for a network.
 * @author ucfoh
 * @version 1.0
 */
public class IP implements Comparable<IP> {

    private static final int REQUIRED_NUMBERS = 4;
    private static final int HIGHEST_VALUE = 255;
    private static final long BIT_MASK = 255;
    private static final short FIRST_BIT_SHIFT = 32;
    private static final int SECOND_BIT_SHIFT = 24;
    private static final int NEXT_BIT_SHIFT_STEP = 8;
    private long ip;

    /**
     * The constructor creates a new IP from a point notation but only if the input was valid.
     * @param pointNotation the ip as pointnotaion
     * @throws ParseException if input was invalid
     */
    public IP(final String pointNotation) throws ParseException {

        if (pointNotation == null) {
            throw new ParseException("null is not a valid IP");
        }
        int counter = 0;
        for (int i = 0; i < pointNotation.length(); i++) {
            if (pointNotation.charAt(i) == '.') {
                counter++;
            }
        }
        if (counter != 3) {
            throw new ParseException("not enough or to many dots");
        }
        String[] numbers = pointNotation.split("\\.");
        if (numbers.length != REQUIRED_NUMBERS) {
            throw new ParseException("not enough numbers");
        }
        for (String numberAsString : numbers) {
            if (numberAsString.length() == 0) {
                throw new ParseException("empty block is not allowed");
            }
            if (numberAsString.charAt(0) == '0' && numberAsString.length() != 1) {
                throw new ParseException("leading zeros are not allowed");
            }
            if (numberAsString.charAt(0) == '-') {
                throw new ParseException("negative zero is not allowed");
            }
        }
        for (String number : numbers) {
            int num;
            try {
                num = Integer.parseInt(number);
            } catch (NumberFormatException e) {
                throw new ParseException(("input is not a number"));
            }
            if (num <= HIGHEST_VALUE && num >= 0) {
                this.ip = this.ip | num;
                this.ip = this.ip << NEXT_BIT_SHIFT_STEP;

            } else {
                throw new ParseException("number is out of range");
            }
        }

    }

    @Override
    public String toString() {
        String ipAsString = "" + ((this.ip >> FIRST_BIT_SHIFT) & BIT_MASK);
        for (int i = SECOND_BIT_SHIFT; i > 0; i = i - NEXT_BIT_SHIFT_STEP) {
            ipAsString = ipAsString + "." + ((this.ip >> i) & BIT_MASK);
        }
        return ipAsString;
    }

    @Override
    public int compareTo(IP o) {
        if (this.ip > o.ip) {
            return 1;
        } else if (this.ip == o.ip) {
            return 0;
        } else {
            return -1;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IP ip1 = (IP) o;
        return ip == ip1.ip;
    }

    @Override
    public int hashCode() {
        return (int) ip;
    }
}

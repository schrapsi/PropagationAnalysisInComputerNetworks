package edu.kit.informatik;


import edu.kit.informatik.exceptions.ParseException;

/**
 * the class IP models an IP for a network.
 * @author ucfoh
 * @version 1.0
 */
public class IP implements Comparable<IP> {

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
        if (numbers.length != 4) {
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
            if (num <= 255 && num >= 0) {
                this.ip = this.ip | num;
                this.ip = this.ip << 8;

            } else {
                throw new ParseException("number is out of range");
            }
        }

    }

    @Override
    public String toString() {
        String ipAsString = "" + ((this.ip >> 32) & 255);
        for (int i = 24; i > 0; i = i - 8) {
            ipAsString = ipAsString + "." + ((this.ip >> i) & 255);
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

package de.tudarmstadt.informatik.pet.pet2018client.util;

public enum DecodedSymbol {
    ONE("1"),
    ZERO("0"),
    NOTHING(" ");

    private final String representation;

    private DecodedSymbol(String representation) {
        this.representation = representation;
    }

    @Override
    public String toString() {
        return representation;
    }
}

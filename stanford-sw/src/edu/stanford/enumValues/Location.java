package edu.stanford.enumValues;

/**
 * Stanford Location facet values
 * @author - Shelley Doljack
 */
public enum Location {
  ART_LOCKED,
  CURRICULUM;

  @Override
  public String toString() {
    switch (this) {
      case ART_LOCKED:
        return "Art Locked Stacks";
      case CURRICULUM:
        return "Curriculum Collection";
      default:
        String lc = super.toString().toLowerCase();
        String firstchar = lc.substring(0, 1).toUpperCase();
        return lc.replaceFirst(".{1}", firstchar);
      }
    }
}

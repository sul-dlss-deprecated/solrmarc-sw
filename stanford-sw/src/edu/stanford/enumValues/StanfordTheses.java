package edu.stanford.enumValues;

/**
 * Stanford Dissertations and Theses facet values
 * @author - Shelley Doljack
 */
public enum StanfordTheses {
  BA,
  DMA,
  DOCTORAL,
  EDD,
  EDM,
  EDS,
  ENGINEER,
  JSD,
  JSM,
  LLM,
  MA,
  MASTERS,
  MD,
  MFA,
  MLA,
  MLS,
  MS,
  PHD,
  STUDENT_RPT,
  UGRADHONORS;

  @Override
  public String toString() {
    switch (this) {
      case BA:
        return "Type of degree|Bachelor's|Bachelor of Arts (BA)";
      case DMA:
        return "Type of degree|Doctoral|Doctor of Musical Arts (DMA)";
      case DOCTORAL:
        return "Type of degree|Doctoral|Unspecified";
      case EDD:
        return "Type of degree|Doctoral|Doctor of Education (EdD)";
      case EDM:
        return "Type of degree|Master's|Master of Education (EdM)";
      case EDS:
        return "Type of degree|Other|Educational Specialist (EdS)";
      case ENGINEER:
        return "Type of degree|Master's|Engineer";
      case JSD:
        return "Type of degree|Doctoral|Doctor of the Science of Law (JSD)";
      case JSM:
        return "Type of degree|Master's|Master of the Science of Law (JSM)";
      case LLM:
        return "Type of degree|Master's|Master of Laws (LLM)";
      case MA:
        return "Type of degree|Master's|Master of Arts (MA)";
      case MASTERS:
        return "Type of degree|Master's|Unspecified";
      case MD:
        return "Type of degree|Doctoral|Doctor of Medicine (MD)";
      case MFA:
        return "Type of degree|Master's|Master of Fine Arts (MFA)";
      case MLA:
        return "Type of degree|Master's|Master of Liberal Arts (MLA)";
      case MLS:
        return "Type of degree|Master's|Master of Legal Studies (MLS)";
      case MS:
        return "Type of degree|Master's|Master of Science (MS)";
      case PHD:
        return "Type of degree|Doctoral|Doctor of Philosophy (PhD)";
      case STUDENT_RPT:
        return "Type of degree|Other|Student report";
      case UGRADHONORS:
        return "Type of degree|Bachelor's|Undergraduate honors thesis";
      default:
        String lc = super.toString().toLowerCase();
        String firstchar = lc.substring(0, 1).toUpperCase();
        return lc.replaceFirst(".{1}", firstchar);
      }
    }
}

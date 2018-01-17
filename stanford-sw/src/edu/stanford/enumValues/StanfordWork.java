package edu.stanford.enumValues;

/**
 * Stanford student work facet values
 * @author - Shelley Doljack
 */
public enum StanfordWork {
  BA,
  DMA,
  DOCTORAL,
  EDD,
  EDM,
  ENGINEER,
  JD,
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
        return "Thesis/Dissertation|Bachelor's|Bachelor of Arts (BA)";
      case DMA:
        return "Thesis/Dissertation|Doctoral|Doctor of Musical Arts (DMA)";
      case DOCTORAL:
        return "Thesis/Dissertation|Doctoral|Unspecified";
      case EDD:
        return "Thesis/Dissertation|Doctoral|Doctor of Education (EdD)";
      case EDM:
        return "Thesis/Dissertation|Master's|Master of Education (EdM)";
      case ENGINEER:
        return "Thesis/Dissertation|Master's|Engineer";
      case JD:
        return "Thesis/Dissertation|Doctoral|Doctor of Jurisprudence (JD)";
      case JSD:
        return "Thesis/Dissertation|Doctoral|Doctor of the Science of Law (JSD)";
      case JSM:
        return "Thesis/Dissertation|Master's|Master of the Science of Law (JSM)";
      case LLM:
        return "Thesis/Dissertation|Master's|Master of Laws (LLM)";
      case MA:
        return "Thesis/Dissertation|Master's|Master of Arts (MA)";
      case MASTERS:
        return "Thesis/Dissertation|Master's|Unspecified";
      case MD:
        return "Thesis/Dissertation|Doctoral|Doctor of Medicine (MD)";
      case MFA:
        return "Thesis/Dissertation|Master's|Master of Fine Arts (MFA)";
      case MLA:
        return "Thesis/Dissertation|Master's|Master of Liberal Arts (MLA)";
      case MLS:
        return "Thesis/Dissertation|Master's|Master of Legal Studies (MLS)";
      case MS:
        return "Thesis/Dissertation|Master's|Master of Science (MS)";
      case PHD:
        return "Thesis/Dissertation|Doctoral|Doctor of Philosophy (PhD)";
      case STUDENT_RPT:
        return "Other student work|Student report";
      case UGRADHONORS:
        return "Thesis/Dissertation|Bachelor's|Undergraduate honors thesis";
      default:
        String lc = super.toString().toLowerCase();
        String firstchar = lc.substring(0, 1).toUpperCase();
        return lc.replaceFirst(".{1}", firstchar);
      }
    }
}

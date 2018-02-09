package org.grobid.core.features;

import org.grobid.core.lexicon.TemporalLexicon;
import org.grobid.core.utilities.TextUtilities;

import java.util.StringTokenizer;
import java.util.regex.Matcher;

/**
 * Class for features used for NER in raw texts.
 *
 * @author Patrice Lopez
 */
public class FeaturesVectorNER {

    public String string = null;     // lexical feature
    public String label = null;     // label if known

    public String capitalisation = null;// one of INITCAP, ALLCAPS, NOCAPS
    public String digit;                // one of ALLDIGIT, CONTAINDIGIT, NODIGIT
    public boolean singleChar = false;

    public String punctType = null;
    // one of NOPUNCT, OPENBRACKET, ENDBRACKET, DOT, COMMA, HYPHEN, QUOTE, PUNCT (default)
    // OPENQUOTE, ENDQUOTE

    // lexical features
    public boolean lastName = false;
    public boolean commonName = false;
    public boolean firstName = false;
    public boolean cityName = false;
    public boolean countryName = false;

    //Match digits corresponding to possible year
    public boolean matchingYearDigits = false;

    //Match multiligual month names
    public boolean matchingMonthNames = false;

    //Match digits corresponding to possible month
    public boolean matchingMonthDigits = false;

    //Match multiligual day names
    public boolean matchingDayNames = false;

    //Match digits corresponding to possible days
    public boolean matchingDayDigits = false;

    public String shadowNumber = null; // Convert digits to “0”

    public String wordShape = null;
    // Convert upper-case letters to "X", lower- case letters to "x", digits to "d" and other to "c"
    // there is also a trimmed variant where sequence of similar character shapes are reduced to one
    // converted character shape
    public String wordShapeTrimmed = null;

    public boolean isLocationToken = false;
    public boolean isPersonTitleToken = false;
    public boolean isOrganisationToken = false;
    public boolean isOrgFormToken = false;

    public FeaturesVectorNER() {
    }

    public String printVector() {
        if (string == null) return null;
        if (string.length() == 0) return null;
        StringBuffer res = new StringBuffer();

        // token string (1)
        res.append(string);

        // lowercase string
        res.append(" " + string.toLowerCase());

        //prefix
        res.append(" " + TextUtilities.prefix(string, 1));
        res.append(" " + TextUtilities.prefix(string, 2));
        res.append(" " + TextUtilities.prefix(string, 3));
        res.append(" " + TextUtilities.prefix(string, 4));
        res.append(" " + TextUtilities.prefix(string, 5));

        //suffix
        res.append(" " + TextUtilities.suffix(string, 1));
        res.append(" " + TextUtilities.suffix(string, 2));
        res.append(" " + TextUtilities.suffix(string, 3));
        res.append(" " + TextUtilities.suffix(string, 4));
        res.append(" " + TextUtilities.suffix(string, 5));

        // capitalisation (1)
        if (digit.equals("ALLDIGIT"))
            res.append(" NOCAPS");
        else
            res.append(" " + capitalisation);

        // digit information (1)
        res.append(" " + digit);

        // character information (1)
        /*if (singleChar)
            res.append(" 1");
        else
            res.append(" 0"); */

        // punctuation information (1)
        //res.append(" " + punctType); // in case the token is a punctuation (NO otherwise)

        // lexical information (10)
        if (lastName)
            res.append(" 1");
        else
            res.append(" 0");

        if (commonName)
            res.append(" 1");
        else
            res.append(" 0");

        if (firstName)
            res.append(" 1");
        else
            res.append(" 0");

        if (cityName)
            res.append(" 1");
        else
            res.append(" 0");

        if (countryName)
            res.append(" 1");
        else
            res.append(" 0");

        if (matchingYearDigits)
            res.append(" 1");
        else
            res.append(" 0");

        if (matchingMonthNames)
            res.append(" 1");
        else
            res.append(" 0");

        if (matchingMonthDigits)
            res.append(" 1");
        else
            res.append(" 0");

        if (matchingDayNames)
            res.append(" 1");
        else
            res.append(" 0");

        if (matchingDayDigits)
            res.append(" 1");
        else
            res.append(" 0");

        
        // lexical feature: belongs to a known location (1)
        if (isLocationToken)
            res.append(" 1");
        else
            res.append(" 0");

        // lexical feature: belongs to a known person title (1)
        if (isPersonTitleToken)
            res.append(" 1");
        else
            res.append(" 0");

        // lexical feature: belongs to a known organisation (1)
        if (isOrganisationToken)
            res.append(" 1");
        else
            res.append(" 0");

        // lexical feature: belongs to a known organisation form (1)
        if (isOrgFormToken)
            res.append(" 1");
        else
            res.append(" 0");

        // token length (1)
        //res.append(" " + string.length()); // /

        // shadow number (1)
        //res.append(" " + shadowNumber); // /

        // word shape (1)
        res.append(" " + wordShape);

        // word shape trimmed (1)
        res.append(" " + wordShapeTrimmed);

        // label - for training data (1)
        if (label != null)
            res.append(" " + label + "");
        else
            res.append(" 0");

        return res.toString();
    }

    public static FeaturesVectorNER addFeaturesNER(String line, boolean isLocationToken, boolean isPersonTitleToken,
                                                   boolean isOrganisationToken, boolean isOrgFormToken) {

        StringTokenizer st = new StringTokenizer(line, "\t ");

        String word = "";
        String label = null;
        if (st.hasMoreTokens()) {
            word = st.nextToken();
            if (st.hasMoreTokens()) {
                label = st.nextToken();
            }
        }

        return addFeaturesNER(word, label, isLocationToken, isPersonTitleToken, isOrganisationToken, isOrgFormToken);
    }

    /**
     * Add the features for the NER model.
     */
    public static FeaturesVectorNER addFeaturesNER(String token, String label,
                                                   boolean isLocationToken, boolean isPersonTitleToken,
                                                   boolean isOrganisationToken, boolean isOrgFormToken) {
        FeatureFactory featureFactory = FeatureFactory.getInstance();

        TemporalLexicon temporalLexicon = TemporalLexicon.getInstance();

        FeaturesVectorNER featuresVector = new FeaturesVectorNER();

        featuresVector.string = token;
        featuresVector.label = label;

        if (token.length() == 1) {
            featuresVector.singleChar = true;
        }

        if (featureFactory.test_all_capital(token)) {
            featuresVector.capitalisation = "ALLCAPS";
        } else if (featureFactory.test_first_capital(token))
            featuresVector.capitalisation = "INITCAP";
        else
            featuresVector.capitalisation = "NOCAPS";

        if (featureFactory.test_number(token))
            featuresVector.digit = "ALLDIGIT";
        else if (featureFactory.test_digit(token))
            featuresVector.digit = "CONTAINDIGIT";
        else
            featuresVector.digit = "NODIGIT";

        Matcher m0 = featureFactory.isPunct.matcher(token);
        if (m0.find()) {
            featuresVector.punctType = "PUNCT";
        }

        if ((token.equals("(")) || (token.equals("["))) {
            featuresVector.punctType = "OPENBRACKET";
        } else if ((token.equals(")")) || (token.equals("]"))) {
            featuresVector.punctType = "ENDBRACKET";
        } else if (token.equals(".")) {
            featuresVector.punctType = "DOT";
        } else if (token.equals(",")) {
            featuresVector.punctType = "COMMA";
        } else if (token.equals("-")) {
            featuresVector.punctType = "HYPHEN";
        } else if (token.equals("\"") | token.equals("\'") | token.equals("`")) {
            featuresVector.punctType = "QUOTE";
        }

        if (featuresVector.capitalisation == null)
            featuresVector.capitalisation = "NOCAPS";

        if (featuresVector.digit == null)
            featuresVector.digit = "NODIGIT";

        if (featuresVector.punctType == null)
            featuresVector.punctType = "NOPUNCT";

        Matcher m2 = temporalLexicon.year.matcher(token);
        if (m2.find()) {
            featuresVector.matchingYearDigits = true;
        }

        featuresVector.matchingMonthNames = temporalLexicon.isMonthNameMatching(token);

        m2 = temporalLexicon.month.matcher(token);
        if (m2.find()) {
            featuresVector.matchingMonthDigits = true;
        }

        featuresVector.matchingDayNames = temporalLexicon.isDayNameMatching(token);

        m2 = temporalLexicon.month.matcher(token);
        if (m2.find()) {
            featuresVector.matchingDayDigits = true;
        }

        if (featureFactory.test_city(token)) {
            featuresVector.cityName = true;
        }

        if (featureFactory.test_country(token)) {
            featuresVector.countryName = true;
        }

        featuresVector.isLocationToken = isLocationToken;

        featuresVector.isPersonTitleToken = isPersonTitleToken;

        featuresVector.isOrganisationToken = isOrganisationToken;

        featuresVector.isOrgFormToken = isOrgFormToken;

        featuresVector.shadowNumber = TextUtilities.shadowNumbers(token);

        featuresVector.wordShape = TextUtilities.wordShape(token);

        featuresVector.wordShapeTrimmed = TextUtilities.wordShapeTrimmed(token);

        return featuresVector;
    }

}

	

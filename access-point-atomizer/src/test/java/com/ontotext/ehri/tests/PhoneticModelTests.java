package com.ontotext.ehri.tests;

import com.ontotext.ehri.phapp.PhoneticModel;
import org.junit.Test;

import java.util.SortedMap;

import static com.ontotext.ehri.phapp.PhoneticModel.*;
import static org.junit.Assert.*;

public class PhoneticModelTests {

    @Test
    public void testScriptIsAllowed() {
        assertTrue("empty string should be allowed", scriptIsAllowed(""));
        assertTrue("space should be allowed", scriptIsAllowed(" "));
        assertTrue("punctuation should be allowed", scriptIsAllowed("?!.:;,-+'\"_()[]{}/\\@#$%^&*"));
        assertTrue("digits should be allowed", scriptIsAllowed("0123456789"));
        assertTrue("basic latin letters should be allowed", scriptIsAllowed("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        assertTrue("special latin letters should be allowed", scriptIsAllowed("äöüßÄÖÜáéíóúýæðþÁÉÍÓÚÝÆÐÞ"));

        assertFalse("cyrillic should not be allowed", scriptIsAllowed("latin кирилица"));
        assertFalse("greek should not be allowed", scriptIsAllowed("latin Επιλεγμένο"));
        assertFalse("hebrew should not be allowed", scriptIsAllowed("latin ברוכים"));
        assertFalse("arabic should not be allowed", scriptIsAllowed("latin مقالة"));
        assertFalse("japanese should not be allowed", scriptIsAllowed("latin 選り抜き記事"));
        assertFalse("chinese should not be allowed", scriptIsAllowed("latin 特色条目"));
    }

    @Test
    public void testSqueeze() {
        assertEquals("repeated letters should be removed", "squeze", squeeze("squeeze"));
        assertEquals("non-letters should be removed", "dont", squeeze("don't"));
        assertEquals("repeated letters and non-letters should be removed", "dontsquezeme", squeeze("don't squeeze meee"));
    }

    @Test
    public void testNormalize() {
        assertEquals("text containing weird scripts should yield empty string", "", normalize("latin 特色条目"));
        assertEquals("special letters should be turned to basic latin letters", "rokfraedi", normalize("rökfræði"));
        assertEquals("uppercase letters should be turned lowercase", "latex", normalize("LaTeX"));
        assertEquals("text should be squeezed as well", "eyjafjalajokulersjotistaerstijokulislands", normalize("Eyjafjallajökull er sjötti stærsti jökull Íslands."));
    }

    @Test
    public void testModel() {
        PhoneticModel phomo = new PhoneticModel(4, 4, 0.5);
        String[] reykjavik = { "Горад Рэйкявік", "ରେକ୍ଜାଭିକ", "Ρευκιαβικ", "ქართული", "रेक्जाविक", "Reykyavik", "Reikiavike", "ਰੇਕਿਆਵਿਕ", "Rejkjaviko", "ریکجاوک", "ريكيافيك", "Reykjawik", "റെയിക്യാവിക്", "雷克雅維克", "Rejkjavik", "རེཀ་ཇ་བིཀ།", "Рейкьявик", "雷克亞維克", "রেইকিয়াভিক", "რეიკიავიკი", "رېيكياۋىك", "Reikjavik", "雷克雅未克", "Рейкиявик", "Reykiavica", "Рейкявік", "ڕێکیاڤیک", "Ρέικιαβικ", "Rēcwīc", "Рејкјавик", "Reiquiavique", "Reiquiavik", "Reikjavīka", "רעקיאוויק", "רייקיאוויק", "Rėikjavėks", "Reiciavicia", "Réicivíc", "ሬይኪያቪክ", "Reykjavik", "Reykjavík", "Reikiavik", "Рейкявик", "రేకవిక్", "レイキャヴィーク", "Reykjavíkur", "ریکیاوک", "Ռեյկյավիկ", "Rèkyavik", "Reikjavikas", "ரெய்க்யவிக்", "रेक्याविक", "เรคยาวิก", "Reykyabik", "레이캬비크", "ریکیاویک" };
        String[] zhitomir = { "Житомир", "ჟიტომირი", "Jytómyr", "Jytomyr", "ז'יטומיר", "Горад Жытомір", "זשיטאמיר", "Žitomir", "日托米尔", "Žõtomõr", "ژیتومیر", "جيتومير", "Zsitomir", "Schytomyr", "ジトームィル", "Жытомир", "Zhytomyr", "Ĵitomir", "Shitomir", "Jitomir", "Zjytomyr", "Żytomierz", "Zhitomir", "Jıtomır", "Ժիտոմիր", "Jîtomîr", "Žytomyr", "Житомиръ", "Zitomiria", "지토미르", "Žytomyras" };
        phomo.add(reykjavik);
        phomo.add(zhitomir);

        SortedMap<String, String> subs = phomo.substitutions(0.05, 0.2);
        String prevSrc = null;
        for (String src : subs.keySet()) {
            if (prevSrc != null) assertTrue("substitutions should be sorted from longest to shortest source", prevSrc.length() >= src.length());
            prevSrc = src;
        }

        verifySubstitution(subs, "que", "k");
        verifySubstitution(subs, "ie", "y");
        verifySubstitution(subs, "c", "k");
        verifySubstitution(subs, "w", "v");
        verifySubstitution(subs, "y", "i");
    }

    private static void verifySubstitution(SortedMap<String, String> substitutions, String source, String target) {
        assertTrue("\"" + source + "\" should be substituted by \"" + target + "\"", target.equals(substitutions.get(source)));
    }
}
